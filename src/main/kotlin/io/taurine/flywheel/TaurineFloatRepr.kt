package io.taurine.flywheel

import dev.engine_room.flywheel.api.layout.FloatRepr
import dev.engine_room.flywheel.backend.compile.component.InstanceAssemblerComponent
import dev.engine_room.flywheel.backend.gl.GlCompat
import dev.engine_room.flywheel.backend.glsl.GlslVersion
import dev.engine_room.flywheel.backend.glsl.generate.GlslExpr
import io.taurine.extension.inaccessible.access

object TaurineFloatRepr {
    @JvmField
    val HALF = FloatRepr.valueOf("HALF")

    val SUPPORTS_F16: Boolean = GlCompat.MAX_GLSL_VERSION >= GlslVersion.V420 || GlCompat.CAPABILITIES.GL_ARB_shading_language_packing

    @JvmStatic
    fun unpackHalf2x16(
        instance: InstanceAssemblerComponent,
        outType: String,
        size: Int,
        byteOffset: Int,
    ): GlslExpr {
        check(SUPPORTS_F16) { "unpackHalf2x16 not supported" }

        val shortOffset = byteOffset / Short.SIZE_BYTES
        val args: MutableList<GlslExpr> = []

        var i = 0
        while (i < size) {
            val wordOffset = (shortOffset + i) / 2
            val vec2 = GlslExpr.call("unpackHalf2x16", instance.access(wordOffset))
            if (i + 1 < size && (shortOffset + i) % 2 == 0) {
                args += vec2.swizzle("xy")
                i += 2
            } else {
                val isHigh = (shortOffset + i) % 2 == 1
                args += vec2.swizzle(if (isHigh) "y" else "x")
                i++
            }
        }
        return GlslExpr.call(outType, args)
    }
}