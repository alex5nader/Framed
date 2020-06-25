package dev.alexnader.framity.util

infix fun <A, B, C> ((A) -> B).andThen(f: (B) -> C): (A) -> C =
    { arg -> f(this(arg)) }
