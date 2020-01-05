package dev.alexnader.framity

val clamp = { min: Float -> { max: Float -> { value: Float -> when {
    value < min -> min
    value > max -> max
    else -> value
}}}}
