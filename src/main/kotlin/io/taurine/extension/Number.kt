package io.taurine.extension

import java.lang.Float.float16ToFloat
import java.lang.Float.floatToFloat16

inline val Int.pixels: Float
    get() = this / 16f

fun Float.toHalf(): Float16 {
    return Float16(floatToFloat16(this))
}

fun Number.toHalf(): Float16 = this.toFloat().toHalf()

@JvmInline
value class Float16 internal constructor(val raw: Short) : Comparable<Float16> {

    override fun compareTo(other: Float16): Int = toFloat().compareTo(other.toFloat())

    operator fun compareTo(other: Int): Int = toFloat().compareTo(other)

    operator fun compareTo(other: Float): Int = toFloat().compareTo(other)

    operator fun compareTo(other: Double): Int = toFloat().compareTo(other)

    operator fun compareTo(other: Long): Int = toFloat().compareTo(other)

    fun toFloat(): Float = float16ToFloat(raw)
}
