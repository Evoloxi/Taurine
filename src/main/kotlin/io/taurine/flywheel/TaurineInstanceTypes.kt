package io.taurine.flywheel

import dev.engine_room.flywheel.api.instance.InstanceType
import dev.engine_room.flywheel.api.layout.FloatRepr
import dev.engine_room.flywheel.api.layout.LayoutBuilder
import dev.engine_room.flywheel.lib.instance.ShadowInstance
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
                .vector("light", FloatRepr.UNSIGNED_SHORT, 2)
                .matrix("pose", FloatRepr.FLOAT, 4)
                .scalar("anchorTime", FloatRepr.FLOAT)
                .vector("motion", TaurineFloatRepr.HALF_FLOAT, 3)
                .build()
        )
        .writer { ptr, instance ->
            ExtraMemoryOps.put2x16(ptr + 0, instance.light)
            ExtraMemoryOps.putMatrix4f(ptr + 4, instance.pose)
            MemoryUtil.memPutFloat(ptr + 68, instance.anchorTime)
            TaurineMemoryOps.memPutVector3f16(ptr + 72, instance.mx, instance.my, instance.mz)
        }
        .build()
}