package dev.alexnader.framity.util

import java.util.*

infix fun <A, B, C> ((A) -> B).andThen(f: (B) -> C): (A) -> C =
    { arg -> f(this(arg)) }

fun <T> Optional<T>.orNull(): T? =
    this.orElse(null)
