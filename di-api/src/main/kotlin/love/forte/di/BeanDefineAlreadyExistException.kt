package love.forte.di

/**
 *
 * 当注册bean，但是bean的name已经存在的时候。
 * @author ForteScarlet
 */
public open class BeanDefineAlreadyExistException : BeansException {
    public constructor() : super()
    public constructor(s: String?) : super(s)
    public constructor(message: String?, cause: Throwable?) : super(message, cause)
    public constructor(cause: Throwable?) : super(cause)
}