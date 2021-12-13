package love.forte.di.core

import love.forte.di.Bean
import love.forte.di.BeanManager
import kotlin.reflect.KClass


/**
 * 基础的BeanManager.
 *
 * @author ForteScarlet
 */
public interface CoreBeanManager : BeanManager {
    override fun register(name: String, bean: Bean<*>)
    override fun getOrNull(name: String): Any?
    override fun <T : Any> getAll(type: KClass<T>): List<String>
    override fun <T : Any> getOrNull(type: KClass<T>): T?
}






/**
 * [CoreBeanManager] 中，每次将要实际注入一个Bean之前都会调用的后置处理器。
 *
 * 会在bean验证名称之前进行处理。
 *
 */
public interface CoreBeanManagerBeanRegisterPostProcessor : Comparable<CoreBeanManagerBeanRegisterPostProcessor> {

    /**
     * 优先级.
     */
    public val priority: Int get() = 100

    /**
     * 得到即将被注册的[Bean], 并返回最终的处理结果。
     * 如果在某个流程中得到null，则终止本次处理。
     */
    public fun process(bean: Bean<*>, beanManager: CoreBeanManager): Bean<*>?


    override fun compareTo(other: CoreBeanManagerBeanRegisterPostProcessor): Int {
        return priority.compareTo(other.priority)
    }
}


/**
 * 根据类型生成对应的BeanName.
 */
public interface BeanNameGenerator {

}

