package love.forte.di

/**
 *
 * Bean名称已经存在异常。
 *
 * @author ForteScarlet
 */
public open class BeanNameAlreadyExistsException : BeansException {
    public constructor() : super()
    public constructor(s: String?) : super(s)
    public constructor(message: String?, cause: Throwable?) : super(message, cause)
    public constructor(cause: Throwable?) : super(cause)
}