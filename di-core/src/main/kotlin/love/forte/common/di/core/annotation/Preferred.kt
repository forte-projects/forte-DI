package love.forte.common.di.core.annotation

import org.springframework.context.annotation.Primary


@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
@Primary
public annotation class Preferred
