package love.forte.common.di

import kotlin.reflect.KClass


/**
 * [Bean] 的注册器，
 * 用于通过对 [Bean] 的 [描述][BeanDescription] 向一个Bean管理器中注册一个 [Bean].
 */
public interface BeanRegistrar {

    /**
     * 注册一个 [Bean]。
     *
     * @throws BeanDefineAlreadyExistException [Bean.name] 已经存在的时候
     */
    public fun register(bean: Bean<*>)
}


/**
 * [Bean] 的容器，可通过此容器并根据名称或类型寻找一个或多个 [Bean].
 */
public interface BeanContainer {

    /**
     * 根据 [Bean] 的唯一名称得到其结果。
     *
     * @throws NoSuchBeanDefineException 当bean不存在时
     */
    public operator fun get(name: String): Bean<*>

    /**
     * 根据 [Bean] 的唯一名称得到其结果, 或者得到null。
     */
    public fun getOrNull(name: String): Bean<*>?


    /**
     * 根据类型获取一个 [Bean]。类型会寻找所有相同类型以及其自类型的所有 [Bean].
     * [Bean] 根据 [Bean.name] 作为唯一限定，因此不能保证同一个类型下只存在一个实例，
     * 更何况类型下可能还存在子类型。
     *
     * @throws NoSuchBeanDefineException 当此类型的bean不存在时
     * @throws MultiSameTypeBeanDefinedException 当此类型的bean存在多个时
     */
    public operator fun <T : Any> get(type: KClass<T>): Bean<T>


    /**
     * 根据类型获取此类型下的所有 [Bean]. 当无法找到任何结果的时候，会返回一个空列表。
     */
    public fun <T : Any> getAll(type: KClass<T>): List<Bean<T>>


    @Api4J
    public fun <T : Any> get(type: Class<T>): Bean<T> = this[type.kotlin]

    @Api4J
    public fun <T : Any> getAll(type: Class<T>): List<Bean<T>> = getAll(type.kotlin)

}


/**
 * 一个Bean管理器，用于管理、获取各种 bean.
 *
 * @author ForteScarlet
 */
public interface BeanManager : BeanRegistrar, BeanContainer


//

