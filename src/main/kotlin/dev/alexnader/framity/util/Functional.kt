package dev.alexnader.framity.util

import java.util.*

fun <T> T.maybe(condition: Boolean, block: T.() -> Unit): T {
    if (condition)
        block()
    return this
}

inline fun <reified K : Enum<K>, V> enumMapOf(vararg pairs: Pair<K, V>): EnumMap<K, V> {
    val map = EnumMap<K, V>(K::class.java)
    pairs.forEach { (k, v) ->
        map[k] = v
    }
    return map
}
