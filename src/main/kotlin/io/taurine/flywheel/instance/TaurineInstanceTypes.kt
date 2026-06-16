package io.taurine.flywheel.instance

import dev.engine_room.flywheel.api.instance.InstanceType
import dev.engine_room.flywheel.api.layout.FloatRepr
import dev.engine_room.flywheel.api.layout.LayoutBuilder
import dev.engine_room.flywheel.lib.instance.ShadowInstance
import dev.engine_room.flywheel.lib.instance.SimpleInstanceType
import dev.engine_room.flywheel.lib.util.ExtraMemoryOps
import dev.engine_room.flywheel.lib.util.ResourceUtil
import io.taurine.Taurine
import io.taurine.flywheel.TaurineFloatRepr
import io.taurine.flywheel.TaurineMemoryOps
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


    val SHADOW_F16: InstanceType<ShadowInstance> =
        SimpleInstanceType.builder(::ShadowInstance)
            .layout(
                LayoutBuilder.create()
                    .vector("pos", FloatRepr.FLOAT, 3)
                    .vector("entityPosXZ", FloatRepr.FLOAT, 2)
                    .vector("size", TaurineFloatRepr.HALF_FLOAT, 2)
                    .scalar("alpha", TaurineFloatRepr.HALF_FLOAT)
                    .scalar("radius", TaurineFloatRepr.HALF_FLOAT)
                    .build()
            )
            .writer { ptr, instance ->
                MemoryUtil.memPutFloat(ptr, instance.x)
                MemoryUtil.memPutFloat(ptr + 4, instance.y)
                MemoryUtil.memPutFloat(ptr + 8, instance.z)
                MemoryUtil.memPutFloat(ptr + 12, instance.entityX)
                MemoryUtil.memPutFloat(ptr + 16, instance.entityZ)
                TaurineMemoryOps.memPutFloat16(ptr + 20, instance.sizeX)
                TaurineMemoryOps.memPutFloat16(ptr + 22, instance.sizeZ)
                TaurineMemoryOps.memPutFloat16(ptr + 24, instance.alpha)
                TaurineMemoryOps.memPutFloat16(ptr + 26, instance.radius)
            }
            .vertexShader(ResourceUtil.rl("instance/shadow.vert"))
            .cullShader(ResourceUtil.rl("instance/cull/shadow.glsl"))
            .build()

}