package io.taurine.extension

import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.core.Vec3i

fun PoseStack.translate(pos: Vec3i) {
    this.translate(pos.x.toFloat(), pos.y.toFloat(), pos.z.toFloat())
}