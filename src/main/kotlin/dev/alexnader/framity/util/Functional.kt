package dev.alexnader.framity.util

import java.util.*

fun <A, B, C, D, E, F, G, H> ((A, B, C, D, E, F, G) -> H).curried() =
    { a: A -> { b: B -> { c: C -> { d: D -> { e: E -> { f: F -> { g: G -> this(a, b, c, d, e, f, g) } } } } } } }
fun <A, B, C, D, E, F, G> ((A, B, C, D, E, F) -> G).curried() =
    { a: A -> { b: B -> { c: C -> { d: D -> { e: E -> { f: F -> this(a, b, c, d, e, f) } } } } } }
fun <A, B, C, D, E, F> ((A, B, C, D, E) -> F).curried() =
    { a: A -> { b: B -> { c: C -> { d: D -> { e: E -> this(a, b, c, d, e) } } } } }
fun <A, B, C, D, E> ((A, B, C, D) -> E).curried() =
    { a: A -> { b: B -> { c: C -> { d: D -> this(a, b, c, d) } } } }
fun <A, B, C, D> ((A, B, C) -> D).curried() =
    { a: A -> { b: B -> { c: C -> this(a, b, c) } } }
fun <A, B, C> ((A, B) -> C).curried() =
    { a: A -> { b: B -> this(a, b) } }

fun <A, B, C, D, E, F, G> ((A) -> (B) -> (C) -> (D) -> (E) -> (F) -> G).uncurried() =
    { a: A, b: B, c: C, d: D, e: E, f: F -> this(a)(b)(c)(d)(e)(f) }
fun <A, B, C, D, E> ((A) -> (B) -> (C) -> (D) -> E).uncurried() =
    { a: A, b: B, c: C, d: D -> this(a)(b)(c)(d) }
fun <A, B, C, D> ((A) -> (B) -> (C) -> D).uncurried() =
    { a: A, b: B, c: C -> this(a)(b)(c) }
fun <A, B, C> ((A) -> (B) -> C).uncurried() =
    { a: A, b: B -> this(a)(b) }

fun <A, B, C, D> ((A) -> (B) -> C).skip(f: ((B) -> C) -> D) =
    { a: A -> f(this(a)) }

infix fun <A, B, C> ((A) -> B).then(next: (B) -> C) = { a: A -> next(this(a)) }

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
