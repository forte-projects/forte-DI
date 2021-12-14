package love.forte.di.core

import love.forte.di.BeanManager
import love.forte.di.core.internal.AnnotationGetter
import love.forte.di.core.internal.CoreBeanClassRegistrarImpl
import kotlin.reflect.KClass

/**
 * bean注册器，通过加载 [KClass]
 *
 * @author ForteScarlet
 */
public interface CoreBeanClassRegistrar {

    /**
     * 注册一个类型到缓冲区中。
     * [types] 中出现的类型默认认为其均为可注入的，不会再检测注解(例如 [love.forte.di.annotation.Beans] ) ,
     *
     * 但是会检测 [love.forte.di.annotation.BeansFactory], 只有存在此注解才会扫描下层的注册函数。
     *
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


public fun coreBeanClassRegistrar(annotationGetter: AnnotationGetter): CoreBeanClassRegistrar =
    CoreBeanClassRegistrarImpl(annotationGetter)