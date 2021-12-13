import java.lang.reflect.AnnotatedElement
import javax.inject.Named
import kotlin.reflect.jvm.javaMethod

public class Foo

fun a(@Named("") p: String){}

fun main() {

    println(::a is AnnotatedElement)

}