package love.forte.di.core

import love.forte.di.BeanManager
import kotlin.reflect.KClass

/**
 * bean注册器，通过加载 [KClass]
 *
 * @author ForteScarlet
 */
public interface CoreBeanClassRegistrar {

    /**
     * 注册一个类型到缓冲区中。
     */
    public fun register(vararg types: KClass<*>): CoreBeanClassRegistrar

    /**
     * 清除当前缓冲区。
     */
    public fun clear()

    /**
     * 将目前已经注册的所有Bean信息解析注入至指定Bean.
     */
    public fun inject(beanManager: BeanManager)
}