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
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.annotation.AliasFor
import javax.annotation.Resource
import javax.inject.Inject
import javax.inject.Named
import kotlin.reflect.KClass


/**
 * 为某个Bean中的属性或者某些函数指定其注入值。
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(
    AnnotationTarget.CONSTRUCTOR,
    AnnotationTarget.FIELD,
    AnnotationTarget.PROPERTY,
    AnnotationTarget.VALUE_PARAMETER,
)
@MustBeDocumented
@Autowired
@AnnotationMapper(value = [Inject::class, Named::class]) // support in forte-di only.
public annotation class Depend(

    /**
     * 指定目标依赖的名称。
     * 在依赖管理中，依赖的名称应当是唯一的，但是需要注意你所需的依赖类型应当与当前的属性类型一致。
     */
    // @get:AliasFor(annotation = Resource::class)
    @Deprecated("Use @Named(...)")
    val name: String = "",

    /**
     * 是否为必须的。如果 [required] 为 false，且在无法断定其最终目标的时候尝试为其注入一个 `null`.
     */
    @Deprecated("无效?")
    val required: Boolean = true

)
