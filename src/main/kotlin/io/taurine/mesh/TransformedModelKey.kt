package io.taurine.mesh

import dev.engine_room.flywheel.api.model.Model
import org.joml.Vector3f

@JvmRecord
data class TransformedModelKey(
    @JvmField val model: Model,
    @JvmField val offset: Vector3f,
    @JvmField val scale: Vector3f
)