package io.taurine.mesh

import dev.engine_room.flywheel.api.model.IndexSequence
import dev.engine_room.flywheel.api.model.Mesh
import dev.engine_room.flywheel.api.model.Model
import dev.engine_room.flywheel.api.model.Model.ConfiguredMesh
import dev.engine_room.flywheel.api.vertex.MutableVertexList
import dev.engine_room.flywheel.lib.model.SimpleModel
import org.joml.Vector3f
import org.joml.Vector4f
import org.joml.Vector4fc
import kotlin.math.max

class TransformedMesh(
    val original: Mesh,
    val offset: Vector3f,
    val scale: Vector3f
) : Mesh {

    companion object {
        private val ZERO = Vector3f()

        @JvmOverloads
        @JvmStatic
        fun transformModel(model: Model, offset: Vector3f, scale: Vector3f = ZERO): SimpleModel {
            return SimpleModel(model.meshes().map {
                ConfiguredMesh(
                    it.material(),
                    TransformedMesh(it.mesh(), offset, scale)
                )
            })
        }
    }

    override fun vertexCount() = original.vertexCount()

    override fun write(vertexList: MutableVertexList) {
        original.write(vertexList)
        for (i in 0..<vertexCount()) {
            with(vertexList) {
                x(i, x(i) * scale.x + offset.x)
                y(i, y(i) * scale.y + offset.y)
                z(i, z(i) * scale.z + offset.z)
            }
        }
    }

    override fun indexSequence(): IndexSequence = original.indexSequence()

    override fun indexCount(): Int = original.indexCount()

    override fun boundingSphere(): Vector4fc {
        val s = original.boundingSphere()
        return Vector4f(
            s.x() * scale.x + offset.x,
            s.y() * scale.y + offset.y,
            s.z() * scale.z + offset.z,
            s.w() * max(scale.x, max(scale.y, scale.z))
        )
    }
}