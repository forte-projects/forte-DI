package love.forte.di.core.internal

import love.forte.di.Bean
import love.forte.di.BeanManager
import love.forte.di.BeansException
import love.forte.di.annotation.BeansFactory
import love.forte.di.annotation.Preferred
import love.forte.di.core.CoreBeanClassRegistrar
import love.forte.di.core.SimpleBean
import org.slf4j.LoggerFactory
import java.util.*
import java.util.function.Supplier
import javax.inject.Inject
import javax.inject.Named
import kotlin.reflect.*
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.instanceParameter
import kotlin.reflect.full.memberFunctions
import kotlin.reflect.full.valueParameters
import kotlin.reflect.jvm.isAccessible
import kotlin.reflect.jvm.jvmName

public interface AnnotationGetter {
    public fun <T : Annotation, R : Any> getAnnotationProperty(
        element: KAnnotatedElement,
        annotationType: KClass<T>,
        name: String,
        propertyType: KClass<R>
    ): R?

    public fun <T : Annotation> containsAnnotation(element: KAnnotatedElement, annotationType: KClass<T>): Boolean
}

/**
 *
 * [CoreBeanClassRegistrar] 基础实现
 *
 * @author ForteScarlet
 */
internal class CoreBeanClassRegistrarImpl(
    internal val annotationGetter: AnnotationGetter
) : CoreBeanClassRegistrar {
    companion object {
        internal val logger = LoggerFactory.getLogger(CoreBeanClassRegistrarImpl::class.java)
        internal val basicTypes = setOf(
            Byte::class, Short::class, Int::class, Long::class,
            Char::class, Boolean::class, Double::class, Float::class,
            String::class
        )
    }


    // 缓冲区
    private val buffer: MutableMap<String, BeanDefinition<*>> = mutableMapOf()


    override fun register(vararg types: KClass<*>): CoreBeanClassRegistrarImpl = also {
        val definitions = types.flatMap { it.toDefinition() }
        for (definition in definitions) {
            println("def name = '${definition.name}': $definition")
            buffer.merge(definition.name, definition) { old, now ->
                throw BeansException("Bean name conflict: $old vs $now")
            }
        }
    }


    private fun <T : Any> KClass<T>.toDefinition(): List<BeanDefinition<*>> {
        val isPreferred = annotationGetter.containsAnnotation(this, Preferred::class)
        val objectInstance = objectInstance
        val currentName = annotationGetter.getAnnotationProperty(this, Named::class, "value", String::class)?.takeIf { it.isNotEmpty() }
        val currentDefinition = if (objectInstance != null) {
            ObjectDefinition(this, currentName, isPreferred)
        } else {
            SimpleClassDefinition(
                type = this,
                isPreferred = isPreferred,
                initName = currentName,
                registrar = this@CoreBeanClassRegistrarImpl
            )
        }

        if (annotationGetter.containsAnnotation(this, BeansFactory::class)) {
            val subList: List<BeanDefinition<*>> = this.memberFunctions.asSequence()
                .filter { func ->
                    annotationGetter.containsAnnotation(func, Named::class)
                }.filter { func ->
                    if (func.visibility != KVisibility.PUBLIC) {
                        logger.warn("Only functions with visibility == PUBLIC can be managed, but {}", func)
                        false
                    } else true
                }.filter { func -> !func.isInline }
                .filter { func ->
                    // 返回值不能是范型, 且必须有返回值.
                    with(func.returnType.classifier) {
                        when {
                            this !is KClass<*> -> {
                                logger.error("The function return type must be an explicit type, but {}", func)
                                false
                            }
                            this == Unit::class -> {
                                logger.error("The function return type must not be Unit, but {}", func)
                                false
                            }
                            this in basicTypes -> {
                                logger.warn(
                                    "The function return type should not be the basic data type or String. but {}",
                                    func
                                )
                                true
                            }
                            else -> true
                        }
                    }
                }.map { func ->
                    if (func.returnType.isMarkedNullable) {
                        throw BeansException("The function return type cannot mark nullable. but $func")
                    }

                    val funcName = annotationGetter.getAnnotationProperty(func, Named::class, "value", String::class)?.takeIf { it.isNotEmpty() }
                    // 函数不允许出现null

                    val funcIsPreferred = annotationGetter.containsAnnotation(func, Preferred::class)

                    @Suppress("UNCHECKED_CAST")
                    SimpleFunctionDefinition(
                        func as KFunction<Any>,
                        currentDefinition.name,
                        funcIsPreferred,
                        funcName,
                        this@CoreBeanClassRegistrarImpl
                    )
                }.toList()

            return mutableListOf<BeanDefinition<*>>(currentDefinition).also { it.addAll(subList) }
        } else {
            return listOf(currentDefinition)
        }
    }


    override fun clear() {
        buffer.clear()
    }

    override fun inject(beanManager: BeanManager) {
        for (value in buffer.values) {
            beanManager.register(value.name, value.toBean(beanManager))
        }
    }
}


private sealed interface BeanDefinition<T : Any> {

    /**
     * name
     */
    val name: String


    /**
     * 将当前定义转化为 [Bean]
     */
    fun toBean(beanManager: BeanManager): Bean<T>
}


/**
 * 将一个 `object` 作为 [BeanDefinition].
 */
private class ObjectDefinition<T : Any>(
    private val type: KClass<T>,
    initName: String? = null,
    private val isPreferred: Boolean,
) : BeanDefinition<T> by SingletonDefinition(
    isPreferred,
    checkNotNull(type.objectInstance) { "Type $type not an object." },
    initName,
    type
) {
    override fun toString(): String {
        return "ObjectDefinition(name=$name, type=$type, isPreferred=$isPreferred)"
    }
}


private class SingletonDefinition<T : Any>(
    private val isPreferred: Boolean,
    private val instance: T,
    initName: String? = null,
    @Suppress("UNCHECKED_CAST")
    private val type: KClass<T> = instance::class as KClass<T>
) : BeanDefinition<T> {
    override val name: String = initName ?: type.qualifiedName ?: type.jvmName

    override fun toBean(beanManager: BeanManager): Bean<T> = SimpleBean(
        type,
        isPreferred,
        // 不需要标记singleton
        isSingleton = false
    ) { instance }

    override fun toString(): String {
        return "SingletonDefinition(name=$name, type=$type, isPreferred=$isPreferred)"
    }
}


/**
 * 一个基础的 definition.
 */
private class SimpleClassDefinition<T : Any>(
    private val type: KClass<T>,
    private val isPreferred: Boolean,
    initName: String? = null,
    private val registrar: CoreBeanClassRegistrarImpl
) : BeanDefinition<T> {
    /**
     * bean实例初始化函数。
     */
    var initializer: (BeanManager) -> () -> T

    /**
     * 通过实例的注入器。
     */
    var injector: (BeanManager, T) -> Unit

    init {
        // 寻找构造
        // 寻找有 @Inject的构造
        val constructors = type.constructors
        val initConstructor = when {
            constructors.isEmpty() -> throw IllegalStateException("No constructor be found in $type.")
            constructors.size == 1 -> constructors.first()
            else -> {
                val needInjects = constructors.filter {
                    registrar.annotationGetter.containsAnnotation(it, Inject::class)
                }
                when {
                    // 构造函数不唯一，但是没有任何标记@Inject的构造函数。
                    needInjects.isEmpty() -> throw IllegalStateException("Constructors are not unique, but there are no constructors marked @Inject or its extensions.")
                    needInjects.size != 1 -> throw IllegalStateException("Constructors are not unique, but there is more than one constructor marked with @Inject or its extensions.")
                    else -> needInjects.first()
                }
            }
        }

        // 构造
        val parameters = initConstructor.parameters
        val parameterSize = parameters.size
        val annotationGetter = registrar.annotationGetter


        //region Initializer init
        // init needed
        val binderList = parameters.map { p -> p.toBinder(annotationGetter) }

        initializer = i@{ manager ->
            return@i {
                val map = HashMap<KParameter, Any?>(parameterSize)
                for (binder in binderList) {
                    binder.include(manager, map)
                }

                initConstructor.callBy(map)
            }
        }
        //endregion

        //region Injector init
        // 扫描所有的属性, 有Inject的就inject.
        val propertiesInjectorList: List<(BeanManager, T) -> Unit> = type.declaredMemberProperties.filter { prop ->
            annotationGetter.containsAnnotation(prop, Inject::class)
        }.map { prop ->
            prop.isAccessible = true

            if (prop is KMutableProperty1) {
                val returnType = prop.returnType
                val propType = prop.returnType.classifier as? KClass<*>
                    ?: throw IllegalStateException("Unable to confirm property type $prop")

                @Suppress("UNCHECKED_CAST")
                prop as KMutableProperty1<T, Any?>
                val name = annotationGetter.getAnnotationProperty(prop, Named::class, "value", String::class)?.takeIf { it.isNotEmpty() }
                println("prop inject name = $name by $prop")
                println(annotationGetter.getAnnotationProperty(prop, Named::class, "value", String::class))
                val nullable = returnType.isMarkedNullable

                val getter = if (name != null) {
                    generateNamedGetterWithSpecialType(name, propType, returnType, nullable, false)
                } else {
                    generateTypedGetterWithSpecialType(propType, returnType, nullable, false)
                }

                if (nullable) {
                    { manager, instance ->
                        prop.set(instance, getter(manager))
                    }
                } else {
                    { manager, instance ->
                        val value = getter(manager)
                        if (value != null) {
                            prop.set(instance, value)
                        } else throw BeansException("Inject for property $prop value was null")
                    }
                }

            } else {
                throw BeansException("Property must be mutable.")
            }
        }

        injector = { manager, instance ->
            for (func in propertiesInjectorList) {
                func(manager, instance)
            }
        }
        //endregion


    }

    override val name: String = initName ?: type.qualifiedName ?: type.jvmName


    override fun toBean(beanManager: BeanManager): Bean<T> {
        val initializerToGetter = initializer(beanManager)
        val injectorForGetter: (T) -> Unit = { instance ->
            injector(beanManager, instance)
        }
        val getter: () -> T = {
            initializerToGetter().also(injectorForGetter)
        }

        return SimpleBean(
            type,
            isPreferred,
            getter = getter
        )
    }


    override fun toString(): String {
        return "SimpleClassDefinition(name=$name, type=$type, isPreferred=$isPreferred)"
    }
}


/**
 * 某factory类型下的子元素
 */
private class SimpleFunctionDefinition<T : Any>(
    private val function: KFunction<T>,
    private val containerName: String,
    private val isPreferred: Boolean,
    initName: String? = null,
    registrar: CoreBeanClassRegistrarImpl
) : BeanDefinition<T> {

    @Suppress("UNCHECKED_CAST")
    private val returnType: KClass<T> = function.returnType.classifier as? KClass<T>
        ?: throw BeansException("Unable to determine function return type: $function")

    private val getterFunc: (BeanManager) -> () -> T

    init {

        val annotationGetter = registrar.annotationGetter

        // 所有所需参数
        val binderList = function.valueParameters.map { p -> p.toBinder(annotationGetter) }
        val instanceParameter = function.instanceParameter

        getterFunc = { manager ->
            if (instanceParameter == null) {
                {
                    val args = HashMap<KParameter, Any?>(binderList.size)
                    binderList.forEach { it.include(manager, args) }
                    function.callBy(args)
                }
            } else {
                {
                    val args = HashMap<KParameter, Any?>(binderList.size + 1)
                    args[instanceParameter] = manager[containerName]
                    binderList.forEach { it.include(manager, args) }
                    function.callBy(args)
                }
            }
        }
    }


    override fun toBean(beanManager: BeanManager): Bean<T> {
        return SimpleBean(
            returnType,
            isPreferred,
            getter = getterFunc(beanManager)
        )
    }

    override val name: String = initName ?: "${containerName}.${function.name}"

    override fun toString(): String {
        return "SimpleFunctionDefinition(name=$name, type=$returnType, container=$containerName, isPreferred=$isPreferred)"
    }
}


private fun KParameter.toParameterType(): ParameterWithType {
    val it = this
    val classifier = it.type.classifier
    if (classifier is KClass<*>) {
        return ParameterWithType(it, classifier)
    } else throw IllegalStateException("Unable to resolve parameter type classifier: $classifier")
}

private fun ParameterWithType.toBinder(annotationGetter: AnnotationGetter): ParameterBinder {
    val p = this
    val parameter = p.parameter
    val optional = parameter.isOptional
    val nullable = parameter.type.isMarkedNullable
    // 是否存在 @Named
    val name = annotationGetter.getAnnotationProperty(p.parameter, Named::class, "value", String::class)?.takeIf { it.isNotEmpty() }
        ?.takeIf { it.isNotEmpty() }
    val getter: (BeanManager) -> Any? = if (name != null) {
        generateNamedGetterWithSpecialType(name, p.type, p.parameter.type, nullable, optional)
    } else {
        // no name. use type
        generateTypedGetterWithSpecialType(p.type, p.parameter.type, nullable, optional)
    }

    return ParameterBinder(parameter, getter)
}


private fun KParameter.toBinder(annotationGetter: AnnotationGetter): ParameterBinder =
    toParameterType().toBinder(annotationGetter)


private fun generateNamedGetterWithSpecialType(
    name: String, type: KClass<*>, kType: KType, nullable: Boolean, optional: Boolean
): (BeanManager) -> Any? {
    return when (type.qualifiedName) {
        "java.util.Optional" -> {
            val first = kType.arguments.first()
            first.type
            val opType = first.type?.classifier as? KClass<*>
                ?: throw BeansException("Unable to determine the type in Optional<$first>")

            when {
                optional -> {
                    { manager ->
                        manager.getOrNull(name, opType)?.let { Optional.of(it) } ?: IgnoreMark
                    }
                }
                // always nullable.
                else -> {
                    { manager ->
                        Optional.ofNullable(manager.getOrNull(name, opType))
                    }
                }
            }
        }

        "kotlin.Function0" -> {
            val first = kType.arguments.first()
            val firstType = first.type!!
            val fun0Type = firstType.classifier as? KClass<*>
                ?: throw BeansException("Unable to determine the type in () -> $first")
            val resultNullable = firstType.isMarkedNullable

            when {
                optional -> {
                    if (resultNullable) {
                        { manager ->
                            val func0: () -> Any? = {
                                manager.getOrNull(name, fun0Type)
                            }
                            func0
                        }
                    } else {
                        // result not null
                        { manager ->
                            val func0: (() -> Any)? = if (name in manager) {
                                {
                                    manager[name, fun0Type]
                                }
                            } else null

                            func0 ?: IgnoreMark
                        }
                    }
                }
                nullable -> {
                    if (resultNullable) {
                        { manager ->
                            val func0: () -> Any? = {
                                manager.getOrNull(name, fun0Type)
                            }
                            func0
                        }
                    } else {
                        { manager ->
                            val func0: (() -> Any)? = if (name in manager) {
                                {
                                    manager[name, fun0Type]
                                }
                            } else null
                            func0
                        }
                    }
                }
                else -> {
                    { manager ->
                        val func0: () -> Any = {
                            manager[name, fun0Type]
                        }
                        func0
                    }
                }
            }
        }

        "java.util.function.Supplier" -> {
            val first = kType.arguments.first()
            val firstType = first.type!!
            val supplierType = firstType.classifier as? KClass<*>
                ?: throw BeansException("Unable to determine the type in Supplier<$first>")
            val resultNullable = firstType.isMarkedNullable

            when {
                optional -> {
                    if (resultNullable) {
                        { manager ->
                            val func0: Supplier<Any?> = Supplier {
                                manager.getOrNull(name, supplierType)
                            }
                            func0
                        }
                    } else {
                        // result not null
                        { manager ->
                            val func0: Supplier<Any>? = if (name in manager) {
                                Supplier {
                                    manager[name, supplierType]
                                }
                            } else null

                            func0 ?: IgnoreMark
                        }
                    }
                }
                nullable -> {
                    if (resultNullable) {
                        { manager ->
                            val func0: Supplier<Any?> = Supplier {
                                manager.getOrNull(name, supplierType)
                            }
                            func0
                        }
                    } else {
                        { manager ->
                            val func0: (Supplier<Any>)? = if (name in manager) {
                                Supplier {
                                    manager[name, supplierType]
                                }
                            } else null
                            func0
                        }
                    }
                }
                else -> {
                    { manager ->
                        val func0: Supplier<Any> = Supplier {
                            manager[name, supplierType]
                        }
                        func0
                    }
                }
            }
        }

        else -> when {
            optional -> {
                { manager ->
                    manager.getOrNull(name, type) ?: IgnoreMark
                }
            }
            nullable -> {
                { manager ->
                    manager.getOrNull(name, type)
                }
            }
            else -> {
                { manager ->
                    manager[name, type]
                }
            }
        }
    }
}

private fun generateTypedGetterWithSpecialType(
    type: KClass<*>, kType: KType, nullable: Boolean, optional: Boolean
): (BeanManager) -> Any? {
    return when (type.qualifiedName) {
        "java.util.Optional" -> {
            val first = kType.arguments.first()
            first.type
            val opType = first.type?.classifier as? KClass<*>
                ?: throw BeansException("Unable to determine the type in Optional<$first>")

            when {
                optional -> {
                    { manager ->
                        manager.getOrNull(opType)?.let { Optional.of(it) } ?: IgnoreMark
                    }
                }
                // always nullable.
                else -> {
                    { manager ->
                        Optional.ofNullable(manager.getOrNull(opType))
                    }
                }
            }
        }

        "kotlin.Function0" -> {
            val first = kType.arguments.first()
            val firstType = first.type!!
            val fun0Type = firstType.classifier as? KClass<*>
                ?: throw BeansException("Unable to determine the type in () -> $first")
            val resultNullable = firstType.isMarkedNullable

            when {
                optional -> {
                    if (resultNullable) {
                        { manager ->
                            val func0: () -> Any? = {
                                manager.getOrNull(fun0Type)
                            }
                            func0
                        }
                    } else {
                        // result not null
                        { manager ->

                            val func0: (() -> Any)? = if (manager.getOrNull(fun0Type) != null) {
                                {
                                    manager[fun0Type]
                                }
                            } else null

                            func0 ?: IgnoreMark
                        }
                    }
                }
                nullable -> {
                    if (resultNullable) {
                        { manager ->
                            val func0: () -> Any? = {
                                manager.getOrNull(fun0Type)
                            }
                            func0
                        }
                    } else {
                        { manager ->
                            val func0: (() -> Any)? = if (manager.getOrNull(fun0Type) != null) {
                                {
                                    manager[fun0Type]
                                }
                            } else null
                            func0
                        }
                    }
                }
                else -> {
                    { manager ->
                        val func0: () -> Any = {
                            manager[fun0Type]
                        }
                        func0
                    }
                }
            }
        }

        "java.util.function.Supplier" -> {
            val first = kType.arguments.first()
            val firstType = first.type!!
            val supplierType = firstType.classifier as? KClass<*>
                ?: throw BeansException("Unable to determine the type in Supplier<$first>")
            val resultNullable = firstType.isMarkedNullable

            when {
                optional -> {
                    if (resultNullable) {
                        { manager ->
                            val func0: Supplier<Any?> = Supplier {
                                manager.getOrNull(supplierType)
                            }
                            func0
                        }
                    } else {
                        // result not null
                        { manager ->
                            val func0: Supplier<Any>? = if (manager.getOrNull(supplierType) != null) {
                                Supplier {
                                    manager[supplierType]
                                }
                            } else null

                            func0 ?: IgnoreMark
                        }
                    }
                }
                nullable -> {
                    if (resultNullable) {
                        { manager ->
                            val func0: Supplier<Any?> = Supplier {
                                manager.getOrNull(supplierType)
                            }
                            func0
                        }
                    } else {
                        { manager ->
                            val func0: (Supplier<Any>)? = if (manager.getOrNull(supplierType) != null) {
                                Supplier {
                                    manager[supplierType]
                                }
                            } else null
                            func0
                        }
                    }
                }
                else -> {
                    { manager ->
                        val func0: Supplier<Any> = Supplier {
                            manager[supplierType]
                        }
                        func0
                    }
                }
            }
        }

        else -> when {
            optional -> {
                { manager ->
                    manager.getOrNull(type) ?: IgnoreMark
                }
            }
            nullable -> {
                { manager ->
                    manager.getOrNull(type)
                }
            }
            else -> {
                { manager ->
                    manager[type]
                }
            }
        }
    }

}


private data class ParameterWithType(
    val parameter: KParameter,
    val type: KClass<*>
)


private data class ParameterBinder(
    val parameter: KParameter,
    val getter: (BeanManager) -> Any?
) {

    fun include(beanManager: BeanManager, map: MutableMap<KParameter, Any?>) {
        val instance = getter(beanManager)
        if (instance !== IgnoreMark) {
            map[parameter] = instance
        }
    }
}

private object IgnoreMark