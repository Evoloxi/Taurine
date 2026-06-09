package io.taurine.extension

inline val Int.pixels: Float
    get() = this / 16f

fun Float.toHalf(): Short {
    return java.lang.Float.floatToFloat16(this)
}