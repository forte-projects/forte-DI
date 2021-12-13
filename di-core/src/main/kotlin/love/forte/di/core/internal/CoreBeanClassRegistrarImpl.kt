package love.forte.di.core.internal

import love.forte.di.Bean
import love.forte.di.BeanManager
import love.forte.di.core.CoreBeanClassRegistrar
import love.forte.di.core.SimpleBean
import javax.inject.Inject
import javax.inject.Named
import kotlin.reflect.KAnnotatedElement
import kotlin.reflect.KClass
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KParameter
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
    lateinit var initializer: (BeanManager) -> () -> T

    /**
     * 通过实例的注入器。
     */
    lateinit var injector: (BeanManager, T) -> Unit

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
        val typeParameters = initConstructor.typeParameters
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
                when {
                    optional -> {
                        { manager ->
                            manager.getOrNull(name, p.type) ?: IgnoreMark
                        }
                    }
                    nullable -> {
                        { manager ->
                            manager.getOrNull(name, p.type)
                        }
                    }
                    else -> {
                        { manager ->
                            manager[name, p.type]
                        }
                    }
                }
            } else {
                // no name. use type
                when {
                    optional -> {
                        { manager ->
                            manager.getOrNull(p.type) ?: IgnoreMark
                        }
                    }
                    nullable -> {
                        { manager ->
                            manager.getOrNull(p.type)
                        }
                    }
                    else -> {
                        { manager ->
                            manager[p.type]
                        }
                    }
                }
            }

            return ParameterBinder(parameter, getter)
        }

        //region Initializer init
        // init needed
        val parameterTypes = parameters.map(KParameter::toParameterType)

        val binderList = parameterTypes.map(ParameterWithType::toBinder)

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
        // 扫描所有的属性, 要有Inject
        // TODO
        val propertiesInjectorList: List<(BeanManager, T) -> Unit> = type.declaredMemberProperties.filter { prop ->
            annotationGetter.containsAnnotation(prop, Inject::class)
        }.map { prop ->
            if (prop is KMutableProperty1) {
                TODO()

            } else {
                throw IllegalStateException("Property must be mutable.")
            }
        }

        injector = TODO()
        //endregion
    }


    override fun toBean(beanManager: BeanManager): Bean<T> {
        TODO("Not yet implemented")
    }

    override val name: String = type.qualifiedName ?: type.jvmName
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