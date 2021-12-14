package test1

import love.forte.di.annotation.Beans
import love.forte.di.annotation.BeansFactory
import love.forte.di.annotation.Preferred
import javax.inject.Inject
import javax.inject.Named


@Beans
@BeansFactory
class Foo {

    @Preferred
    @Beans
    fun bar1() = Bar.BarA("Forte")

    @Beans(value = "bar-age")
    fun bar2() = Bar.BarB("forli", 16)

}

interface Bar {
    val name: String

    @Beans
    data class BarA(override val name: String) : Bar
    @Beans
    data class BarB(override val name: String, val age: Int) : Bar
}


interface Service {

    val str: String

}


@Named("my-service")
class MyServiceImpl(
    val bar: Bar,
) : Service {

    @Inject
    @Named("bar-age")
    lateinit var barAge: Bar

    override val str: String
        get() = "bar=$bar, barAge=$barAge"
}