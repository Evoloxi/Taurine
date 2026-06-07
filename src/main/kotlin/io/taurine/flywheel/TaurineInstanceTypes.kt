package io.taurine.flywheel

import dev.engine_room.flywheel.api.layout.FloatRepr
import dev.engine_room.flywheel.api.layout.IntegerRepr
import dev.engine_room.flywheel.api.layout.LayoutBuilder
import dev.engine_room.flywheel.lib.instance.SimpleInstanceType
import dev.engine_room.flywheel.lib.util.ExtraMemoryOps
import dev.engine_room.flywheel.lib.util.ResourceUtil
import io.taurine.Taurine
import org.lwjgl.system.MemoryUtil

object TaurineInstanceTypes {
    val CONSTANT_MOTION: SimpleInstanceType<ConstantMotionInstance> = SimpleInstanceType.builder(::ConstantMotionInstance)
        .cullShader(Taurine("instance/cull/constant_motion.glsl"))
        .vertexShader(Taurine("instance/constant_motion.vert"))
        .layout(
            LayoutBuilder.create()
                .vector("color", FloatRepr.NORMALIZED_UNSIGNED_BYTE, 4)
                .vector("overlay", IntegerRepr.SHORT, 2)
                .vector("light", FloatRepr.UNSIGNED_SHORT, 2)
                .matrix("pose", FloatRepr.FLOAT, 4)
                .vector("motion", FloatRepr.FLOAT, 3)
                .scalar("anchorTime", FloatRepr.FLOAT)
                .build()
        )
        .writer { ptr, instance ->
            MemoryUtil.memPutByte(ptr, instance.red)
            MemoryUtil.memPutByte(ptr + 1, instance.green)
            MemoryUtil.memPutByte(ptr + 2, instance.blue)
            MemoryUtil.memPutByte(ptr + 3, instance.alpha)
            ExtraMemoryOps.put2x16(ptr + 4, instance.overlay)
            ExtraMemoryOps.put2x16(ptr + 8, instance.light)
            ExtraMemoryOps.putMatrix4f(ptr + 12, instance.pose)
            MemoryUtil.memPutFloat(ptr + 76, instance.mx)
            MemoryUtil.memPutFloat(ptr + 80, instance.my)
            MemoryUtil.memPutFloat(ptr + 84, instance.mz)
            MemoryUtil.memPutFloat(ptr + 88, instance.anchorTime)
        }
        .build()
}