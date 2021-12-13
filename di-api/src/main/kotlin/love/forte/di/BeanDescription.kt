package love.forte.di

import kotlin.reflect.KClass

/**
 *
 * 这是一个对于 [Bean] 的描述。
 *
 *  @author ForteScarlet
 */
public interface BeanDescription {

    public val type: KClass<*>


}