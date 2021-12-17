package love.forte.di.annotation

import love.forte.annotationtool.AnnotationMapper
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.annotation.AliasFor
import org.springframework.stereotype.Component
import javax.inject.Named

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
@Named
public annotation class Beans(
    /** 在spring环境下，标记在类上时可使用此参数 */
    @get:AliasFor(annotation = Component::class, value = "value")
    val parentBeanName: String = "",

    /** 在spring环境下，标记在类上时使用此参数, 或者使用普通环境时使用此参数。 */
    @get:AliasFor(annotation = Component::class, value = "value")
    @get:AnnotationMapper.Property(target = Named::class, value = "value")
    val value: String = "",

    /** 在spring环境下，标记在 [Configuration] 下的函数上时使用此参数, 对应 [Bean] 注解参数 */
    @get:AliasFor(annotation = Bean::class, attribute = "value")
    val childBeanName: Array<String> = [],

    /**
     * 优先级，仅在 forte-di 中有效。
     */
    val priority: Int = 1000
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


@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER)
@Component // for spring
@ConditionalOnMissingBean  // for spring
@Beans(priority = Int.MAX_VALUE)
public annotation class SpareBean
