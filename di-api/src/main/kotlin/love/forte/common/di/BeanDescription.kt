package love.forte.common.di

import javax.inject.Provider
import kotlin.reflect.KClass

/**
 *
 * 这是一个对于 [Bean] 的描述。
 * 通过 [BeanDescription] 来向一个 [BeanDescription] 中注册结果。
 *
 * @author ForteScarlet
 */
public interface BeanDescription {

    public val name: String

    public val type: KClass<*>



}