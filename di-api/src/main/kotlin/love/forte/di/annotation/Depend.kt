/*
 *  Copyright (c) 2021-2021 ForteScarlet <https://github.com/ForteScarlet>
 *
 *  根据 Apache License 2.0 获得许可；
 *  除非遵守许可，否则您不得使用此文件。
 *  您可以在以下网址获取许可证副本：
 *
 *       https://www.apache.org/licenses/LICENSE-2.0
 *
 *   有关许可证下的权限和限制的具体语言，请参见许可证。
 */

package love.forte.di.annotation

import love.forte.annotationtool.AnnotationMapper
import javax.inject.Inject


/**
 * 为某个Bean中的属性或者某些函数指定其注入值。
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(
    AnnotationTarget.CONSTRUCTOR,
    AnnotationTarget.FIELD,
    AnnotationTarget.PROPERTY,
    AnnotationTarget.PROPERTY_GETTER,
    AnnotationTarget.PROPERTY_SETTER,
    AnnotationTarget.VALUE_PARAMETER,
)
@MustBeDocumented
@AnnotationMapper(value = [Inject::class]) // support in forte-di only.
public annotation class Depend(

    /**
     * 无效的参数。
     */
    @Deprecated("Use @Named(...)")
    val name: String = "",

    /**
     * 是否为必须的。如果 [required] 为 false，且在无法断定其最终目标的时候尝试为其注入一个 `null`.
     */
    val required: Boolean = true

)
