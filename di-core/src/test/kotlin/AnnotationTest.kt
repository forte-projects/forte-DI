import java.util.*
import javax.inject.Inject
import kotlin.reflect.KCallable
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.full.isSubtypeOf
import kotlin.reflect.full.primaryConstructor

class Foo<T> (@Inject val nameFactory: () -> String?) {


}

fun f1(): Any = "a"
fun f2(): Any? = null

fun main() {

    val pc = Foo::class.primaryConstructor!!



    for (p in pc.parameters) {
        for (argument in p.type.arguments) {
            println(argument)
            println(argument.type)
            println(argument.type?.isMarkedNullable)
            println(argument.type?.classifier!!::class)
        }

        println("--------------")
    }

    val fuc1 = ::f1
    val fuc2 = ::f2

    println(pc.call(fuc1).nameFactory()?.length)

    println(pc.call(fuc2).nameFactory()?.length)


}