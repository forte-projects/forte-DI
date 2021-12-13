package love.forte.di.annotation

import org.springframework.context.annotation.Primary

/**
 * 标记一个注入对象为主要目标。
 * 当在bean管理中存在多个相似类型的时候（例如某类型的多个子类或子类实例），通过标记一个 [Preferred] 来指定一个**主要**的目标。
 *
 * 一次类型获取中，主要目标应至多一个。
 *
 * @see love.forte.di.Bean.isPreferred
 *
 */
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
@Primary // for spring
public annotation class Preferred
