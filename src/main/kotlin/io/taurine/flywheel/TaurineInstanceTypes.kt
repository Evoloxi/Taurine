package io.taurine.flywheel

import dev.engine_room.flywheel.api.layout.FloatRepr
import dev.engine_room.flywheel.api.layout.LayoutBuilder
import dev.engine_room.flywheel.lib.instance.SimpleInstanceType
import dev.engine_room.flywheel.lib.util.ExtraMemoryOps
import io.taurine.Taurine
import io.taurine.extension.toHalf
import org.lwjgl.system.MemoryUtil

object TaurineInstanceTypes {
    val CONSTANT_MOTION: SimpleInstanceType<ConstantMotionInstance> = SimpleInstanceType.builder(::ConstantMotionInstance)
        .cullShader(Taurine("instance/cull/constant_motion.glsl"))
        .vertexShader(Taurine("instance/constant_motion.vert"))
        .layout(
            LayoutBuilder.create()
                .vector("light", FloatRepr.UNSIGNED_SHORT, 2)
                .matrix("pose", FloatRepr.FLOAT, 4)
                .scalar("anchorTime", FloatRepr.FLOAT)
                .vector("motion", TaurineFloatRepr.HALF, 3)
                .build()
        )
        .writer { ptr, instance ->
            ExtraMemoryOps.put2x16(ptr + 0, instance.light)
            ExtraMemoryOps.putMatrix4f(ptr + 4, instance.pose)
            MemoryUtil.memPutFloat(ptr + 68, instance.anchorTime)
            MemoryUtil.memPutShort(ptr + 72, instance.mx.toHalf())
            MemoryUtil.memPutShort(ptr + 74, instance.my.toHalf())
            MemoryUtil.memPutShort(ptr + 76, instance.mz.toHalf())
        }
        .build()
}