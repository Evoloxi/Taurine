package io.taurine.flywheel

import dev.engine_room.flywheel.api.layout.FloatRepr
import dev.engine_room.flywheel.backend.gl.GlCompat
import dev.engine_room.flywheel.backend.glsl.GlslVersion

object TaurineFloatRepr {
    @JvmField
    val HALF_FLOAT = FloatRepr.valueOf("HALF_FLOAT")

    private val SUPPORTS_F16: Boolean = GlCompat.MAX_GLSL_VERSION >= GlslVersion.V420 || GlCompat.CAPABILITIES.GL_ARB_shading_language_packing
}

