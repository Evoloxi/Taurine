package io.taurine.flywheel.gl

import dev.engine_room.flywheel.backend.glsl.generate.GlslExpr
import dev.engine_room.flywheel.backend.glsl.generate.GlslStmt

interface TaurineGlslStmt : GlslStmt {

    @JvmRecord
    private data class Declare(
        @JvmField val type: String,
        @JvmField val name: String,
        @JvmField val expr: GlslExpr
    ) : GlslStmt {
        override fun prettyPrint(): String = "$type $name = ${expr.prettyPrint()};"
    }

    companion object {
        @JvmStatic
        fun declare(type: String, name: String, value: GlslExpr): GlslStmt {
            return Declare(type, name, value)
        }
    }
}