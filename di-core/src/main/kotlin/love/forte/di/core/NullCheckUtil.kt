package love.forte.di.core

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

@OptIn(ExperimentalContracts::class)
internal inline fun <T> T?.ifNull(msg: () -> String): T {
    contract {
        returns() implies (this@ifNull != null)
    }
    if (this == null) {
        throw NullPointerException(msg())
    } else {
        return this
    }
}