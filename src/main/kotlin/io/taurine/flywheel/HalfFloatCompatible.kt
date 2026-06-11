package io.taurine.flywheel

import dev.engine_room.flywheel.backend.glsl.generate.GlslBlock
import dev.engine_room.flywheel.backend.glsl.generate.GlslExpr

@Suppress("FunctionName", "PropertyName")
interface HalfFloatCompatible {
    var `taurine$body`: GlslBlock?

    fun `taurine$unpackHalfFloatScalar`(shortOffset: Int, body: GlslBlock?): GlslExpr

    fun `taurine$halfTempForWord`(wordOffset: Int, body: GlslBlock?): GlslExpr

    fun `taurine$unpackHalfFloatVector`(outType: String, size: Int, shortOffset: Int, body: GlslBlock?): GlslExpr
}