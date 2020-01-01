package dev.alexnader.framity.util

infix fun <A, B, C> ((A) -> B).then(next: (B) -> C) = { a: A -> next(this(a)) }

fun <T> T.maybe(condition: Boolean, block: T.() -> Unit): T {
    if (condition)
        block()
    return this
}
