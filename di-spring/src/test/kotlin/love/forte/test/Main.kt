package love.forte.test

import love.forte.di.annotation.Beans
import love.forte.di.annotation.BeansFactory
import love.forte.di.annotation.Preferred
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import javax.inject.Inject
import javax.inject.Named

@SpringBootApplication
open class Main

fun main(vararg args: String) {
    runApplication<Main>(*args)
}


@RestController
open class TestController {

    @Autowired
    private lateinit var myBean: MyBean

    @Autowired
    private lateinit var util: AbcUtil

    @GetMapping("/test")
    fun test(): List<String> {
        println(myBean)
        println(util)
        return listOf(
            myBean.name,
            util.bean1.name,
            util.bean2.name,
        )

    }

}


interface MyBean {
    val name: String
}

private open class MyBeanImpl(override val name: String) : MyBean
private open class MyBeanImpl2(override val name: String) : MyBean

@BeansFactory
open class BeanConfig {

    @Preferred
    @Beans
    open fun myBean(): MyBean = MyBeanImpl("Forte")

    @Beans(childBeanName = ["b2"])
    open fun myBean2(): MyBean = MyBeanImpl2("Forli")
}

@Beans
open class AbcUtil {

    @Inject
    lateinit var bean1: MyBean

    @set:Inject
    @set:Named("b2")
    lateinit var bean2: MyBean

}