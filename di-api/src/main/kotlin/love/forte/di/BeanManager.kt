package love.forte.di

import kotlin.reflect.KClass
import kotlin.reflect.cast


/**
 * [Bean] 的注册器，
 * 用于通过对 [Bean] 的 [描述][BeanDescription] 向一个Bean管理器中注册一个 [Bean].
 */
public interface BeanRegistrar {

    /**
     * 注册一个 [Bean]。
     *
     * @throws BeanDefineAlreadyExistException [name] 已经存在的时候
     */
    public fun register(name: String, bean: Bean<*>)
}


/**
 * [Bean] 的容器，可通过此容器并根据名称或类型寻找一个或多个 [Bean].
 */
public interface BeanContainer {

    /**
     * 根据 [Bean] 的唯一名称得到其结果。
     *
     * @throws NoSuchBeanException 当bean不存在时
     */
    public operator fun get(name: String): Any = getOrNull(name) ?: noSuchBeanDefine { name }

    /**
     * 检测是否存在某个bean name。
     */
    public operator fun contains(name: String): Boolean

    /**
     * 根据的唯一限定0名称得到其结果, 或者得到null。
     */
    public fun getOrNull(name: String): Any?

    /**
     * 根据 [Bean] 的唯一名称得到其结果。
     *
     * @throws NoSuchBeanException 当bean不存在时
     * @throws ClassCastException 类型不匹配时
     */
    public operator fun <T : Any> get(name: String, type: KClass<T>): T = type.cast(this[name])

    /**
     * 根据的唯一限定0名称得到其结果, 或者得到null。
     *
     * @throws ClassCastException 类型不匹配时
     */
    public fun <T : Any> getOrNull(name: String, type: KClass<T>): T? = getOrNull(name)?.let(type::cast)


    @Api4J
    public operator fun <T : Any> get(name: String, type: Class<T>): T = type.cast(this[name])

    @Api4J
    public fun <T : Any> getOrNull(name: String, type: Class<T>): T? = getOrNull(name)?.let(type::cast)


    /**
     * 根据类型获取一个 [Bean]。类型会寻找所有相同类型以及其自类型的所有 [Bean].
     * [Bean] 根据 `name` 作为唯一限定，因此不能保证同一个类型下只存在一个实例，
     * 更何况类型下可能还存在子类型。
     *
     * @throws NoSuchBeanException 当此类型的bean不存在时
     * @throws MultiSameTypeBeanException 当此类型的bean存在多个时
     */
    public operator fun <T : Any> get(type: KClass<T>): T =
        getOrNull(type) ?: throw NoSuchBeanException("type of $type")


    /**
     * 根据类型获取一个 [Bean]。类型会寻找所有相同类型以及其自类型的所有 [Bean].
     * [Bean] 根据 `name` 作为唯一限定，因此不能保证同一个类型下只存在一个实例，
     * 更何况类型下可能还存在子类型。
     *
     * @throws MultiSameTypeBeanException 当此类型的bean存在多个时
     */
    public fun <T : Any> getOrNull(type: KClass<T>): T?


    /**
     * 根据类型获取此类型下的所有 [Bean] 的名称. 当无法找到任何结果的时候，会返回一个空列表。
     */
    public fun <T : Any> getAll(type: KClass<T>): List<String>


    @Api4J
    public operator fun <T : Any> get(type: Class<T>): T = this[type.kotlin]

    @Api4J
    public fun <T : Any> getOrNull(type: Class<T>): T? = getOrNull(type.kotlin)

    @Api4J
    public fun <T : Any> getAll(type: Class<T>): List<String> = getAll(type.kotlin)


    public companion object Empty : BeanContainer {
        override fun contains(name: String): Boolean = false
        override fun getOrNull(name: String): Any? = null
        override fun <T : Any> getOrNull(type: KClass<T>): T? = null
        override fun <T : Any> getAll(type: KClass<T>): List<String> = emptyList()
    }
}

/**
 * 存在嵌套关系的 [BeanContainer], 可能存在一个 [上层容器][parentContainer].
 */
public interface HierarchicalBeanContainer : BeanContainer {

    /**
     * 父级容器
     */
    public val parentContainer: BeanContainer?


    /**
     * 检测在**当前**容器中是否存在指定名称的bean。
     */
    public fun containsLocal(name: String): Boolean


    public companion object Empty : HierarchicalBeanContainer, BeanContainer by BeanContainer {
        override val parentContainer: BeanContainer? get() = null
        override fun containsLocal(name: String): Boolean = false
    }
}


/**
 * 一个Bean管理器，用于管理、获取各种 bean.
 *
 * @author ForteScarlet
 */
public interface BeanManager : BeanRegistrar, BeanContainer


//

