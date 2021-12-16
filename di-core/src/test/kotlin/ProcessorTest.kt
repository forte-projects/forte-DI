import love.forte.di.core.coreBeanClassRegistrar
import love.forte.di.core.coreBeanManager
import love.forte.di.postValue
import test1.AnnoGetter

class B
data class A(val b: B)

fun main() {

    val m = coreBeanManager {
        plusProcessor { b, _ ->
            b.postValue { _, value ->
                println("Value: $value")
                value
            }
        }
    }

    val registrar = coreBeanClassRegistrar(annotationGetter = AnnoGetter)
    registrar.register(A::class, B::class)
    registrar.inject(m)

    println(m[B::class])
    println("111")
    println(m[A::class])
}