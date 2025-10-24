package io.taurine.flywheel

import com.simibubi.create.Create
import dev.engine_room.flywheel.api.layout.FloatRepr
import dev.engine_room.flywheel.api.layout.IntegerRepr
import dev.engine_room.flywheel.api.layout.LayoutBuilder
import dev.engine_room.flywheel.lib.instance.SimpleInstanceType
import dev.engine_room.flywheel.lib.util.ExtraMemoryOps
import io.taurine.Taurine
import org.lwjgl.system.MemoryUtil

object TaurineInstanceTypes {
    val SCALING_FLUID: SimpleInstanceType<ScalingFluidInstance> = SimpleInstanceType.builder(::ScalingFluidInstance)
		.cullShader(Create.asResource("instance/cull/fluid.glsl"))
		.vertexShader(Taurine("instance/scaling_fluid.vert"))
		.layout(
            LayoutBuilder.create()
			.matrix("pose", FloatRepr.FLOAT, 4)
			.vector("color", FloatRepr.NORMALIZED_UNSIGNED_BYTE, 4)
			.vector("light", IntegerRepr.SHORT, 2)
			.scalar("vScale", FloatRepr.FLOAT)
			.scalar("uScale", FloatRepr.FLOAT)
            .scalar("v0", FloatRepr.FLOAT)
			.scalar("u0", FloatRepr.FLOAT)
			.build())
		.writer { ptr, instance ->
            ExtraMemoryOps.putMatrix4f(ptr, instance.pose)
            MemoryUtil.memPutByte(ptr + 64, instance.red)
            MemoryUtil.memPutByte(ptr + 65, instance.green)
            MemoryUtil.memPutByte(ptr + 66, instance.blue)
            MemoryUtil.memPutByte(ptr + 67, instance.alpha)
            ExtraMemoryOps.put2x16(ptr + 68, instance.light)
            MemoryUtil.memPutFloat(ptr + 72, instance.vScale)
            MemoryUtil.memPutFloat(ptr + 76, instance.uScale)
            MemoryUtil.memPutFloat(ptr + 80, instance.v0)
            MemoryUtil.memPutFloat(ptr + 84, instance.u0)
        }
        .build()
}