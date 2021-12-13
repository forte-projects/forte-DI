package love.forte.di.annotation

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.annotation.AliasFor
import org.springframework.stereotype.Component

/**
 * 标记一个类或一个类下的有返回值的方法或一个属性（的getter）上，代表将其记录在bean管理器中。
 *
 * 当标记在类中属性/函数时，当前类必须标记 @BeansFactory.
 *
 * 在注入时，除了使用 [parentBeanName] 或 [childBeanName] 来指定类型以外，
 * 也可以通过 [javax.inject.Named] 来指定名称。
 *
 * @param parentBeanName 当标记在一个类上时使用此参数指定一个bean名称。
 * @param childBeanName 当标记在类下的函数或属性getter上时使用此参数指定一个bean名称。
 *
 */
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER)
@Component // for spring
@Bean // for spring
public annotation class Beans(
    @get:AliasFor(annotation = Component::class, attribute = "value")
    val parentBeanName: String = "",

    @get:AliasFor(annotation = Bean::class, attribute = "value")
    val childBeanName: Array<String> = []

)

/**
 * 标记一个类为bean工厂，其含义为通过类中属性/函数来产生bean。
 *
 * @param name 可指定此bean的唯一标识
 */
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER)
@Configuration // for spring
public annotation class BeansFactory(
    @get:AliasFor(annotation = Configuration::class, attribute = "value")
    val name: String = ""
)