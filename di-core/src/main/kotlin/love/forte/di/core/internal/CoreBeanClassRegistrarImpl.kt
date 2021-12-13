package love.forte.di.core.internal

import love.forte.di.Bean
import love.forte.di.BeanManager
import love.forte.di.BeansException
import love.forte.di.core.CoreBeanClassRegistrar
import love.forte.di.core.SimpleBean
import java.util.*
import java.util.function.Supplier
import javax.inject.Inject
import javax.inject.Named
import kotlin.reflect.*
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.jvm.jvmName

public interface AnnotationGetter {
    public fun <T : Annotation> getAnnotation(element: KAnnotatedElement, annotationType: KClass<T>): T?
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


    // 缓冲区
    private val buffer: MutableMap<String, BeanDefinition<*>> = mutableMapOf()


    override fun register(vararg types: KClass<*>): CoreBeanClassRegistrar {
        TODO("Not yet implemented")
    }

    override fun clear() {
        buffer.clear()
    }

    override fun inject(beanManager: BeanManager) {

        TODO()
    }
}


private interface BeanDefinition<T : Any> {

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
    private val isPreferred: Boolean,
) : BeanDefinition<T> {
    override val name: String = type.qualifiedName ?: type.jvmName
    private val instance: T = checkNotNull(type.objectInstance) { "Type $type not an object." }
    override fun toBean(beanManager: BeanManager) = SimpleBean(
        type,
        isPreferred,
        // 不需要标记singleton
        isSingleton = false
    ) { instance }
}


/**
 * 一个基础的 definition.
 */
private class SimpleClassDefinition<T : Any>(
    private val type: KClass<T>,
    private val isPreferred: Boolean,
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

        fun KParameter.toParameterType(): ParameterWithType {
            val it = this
            val classifier = it.type.classifier
            if (classifier is KClass<*>) {
                return ParameterWithType(it, classifier)
            } else throw IllegalStateException("Unable to resolve parameter type classifier: $classifier")
        }

        fun ParameterWithType.toBinder(): ParameterBinder {
            val p = this
            val parameter = p.parameter
            val optional = parameter.isOptional
            val nullable = parameter.type.isMarkedNullable
            // 是否存在 @Named
            val name = annotationGetter.getAnnotation(p.parameter, Named::class)?.value?.takeIf { it.isNotEmpty() }
            val getter: (BeanManager) -> Any? = if (name != null) {
                generateNamedGetterWithSpecialType(name, p.type, p.parameter.type, nullable, optional)
            } else {
                // no name. use type
                generateTypedGetterWithSpecialType(p.type, p.parameter.type, nullable, optional)
            }

            return ParameterBinder(parameter, getter)
        }

        fun KParameter.toBinder(): ParameterBinder = toParameterType().toBinder()

        //region Initializer init
        // init needed
        val binderList = parameters.map(KParameter::toBinder)

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

            if (prop is KMutableProperty1) {
                val returnType = prop.returnType
                val propType = prop.returnType.classifier as? KClass<*> ?: throw IllegalStateException("Unable to confirm property type $prop")

                @Suppress("UNCHECKED_CAST")
                prop as KMutableProperty1<T, Any?>

                val name = annotationGetter.getAnnotation(prop, Named::class)?.value
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


    override fun toBean(beanManager: BeanManager): Bean<T> {
        TODO("Not yet implemented")
    }

    override val name: String = type.qualifiedName ?: type.jvmName
}


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