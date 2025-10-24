package io.taurine.flywheel.patch

import dev.engine_room.flywheel.api.material.Material
import dev.engine_room.flywheel.backend.engine.indirect.IndirectBuffers
import dev.engine_room.flywheel.backend.gl.GlCompat
import dev.engine_room.flywheel.backend.gl.shader.GlProgram
import org.lwjgl.opengl.GL11.GL_TRIANGLES
import org.lwjgl.opengl.GL11.GL_UNSIGNED_INT

// we love binary compatibility
@JvmRecord
data class MultiDraw(@JvmField val material: Material, @JvmField val embedded: Boolean, val start: Int, val end: Int) {

    fun submit(drawProgram: GlProgram) {
        GlCompat.safeMultiDrawElementsIndirect(
            drawProgram,
            GL_TRIANGLES,
            GL_UNSIGNED_INT,
            this.start,
            this.end,
            IndirectBuffers.DRAW_COMMAND_STRIDE
        )
    }
}