package love.forte.di.spring

import love.forte.di.BeanContainer
import love.forte.di.BeanManager
import love.forte.di.HierarchicalBeanContainer
import org.springframework.beans.factory.BeanFactory
import org.springframework.beans.factory.ListableBeanFactory
import org.springframework.beans.factory.support.BeanDefinitionRegistry
import org.springframework.context.ApplicationContext
import kotlin.reflect.KClass

/**
 *
 * @author ForteScarlet
 */
public interface SpringBeanContainer : BeanContainer {

    /**
     * 内部对应的spring bean factory.
     */
    public val listableBeanFactory: ListableBeanFactory

}


/**
 * 通过 Spring 进行管理的 [BeanManager]
 */
public interface SpringBeanManager : SpringBeanContainer, BeanManager {

    /**
     * 对应的 [BeanDefinitionRegistry].
     */
    public val beanDefinitionRegistry: BeanDefinitionRegistry


}