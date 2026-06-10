package io.taurine.flywheel

import io.taurine.extension.Float16
import io.taurine.extension.toHalf
import org.lwjgl.system.MemoryUtil

object TaurineMemoryOps {
    fun memPutFloat16(ptr: Long, value: Float16) {
        MemoryUtil.memPutShort(ptr, value.raw)
    }
    fun memPutFloat16(ptr: Long, value: Float) {
        memPutFloat16(ptr, value.toHalf())
    }
    fun memPutVector3f16(ptr: Long, x: Float, y: Float, z: Float) {
        memPutFloat16(ptr, x)
        memPutFloat16(ptr + 2, y)
        memPutFloat16(ptr + 4, z)
    }
}