package io.taurine.mesh

import dev.engine_room.flywheel.api.material.Transparency
import dev.engine_room.flywheel.api.material.WriteMask
import dev.engine_room.flywheel.api.vertex.MutableVertexList
import dev.engine_room.flywheel.lib.material.SimpleMaterial
import dev.engine_room.flywheel.lib.model.QuadMesh
import dev.engine_room.flywheel.lib.model.SingleMeshModel
import net.minecraft.client.renderer.texture.OverlayTexture
import net.minecraft.resources.ResourceLocation
import org.joml.Vector4f
import org.joml.Vector4fc
import kotlin.math.sqrt

object ShadowMesh : QuadMesh {
    private val BOUNDING_SPHERE = Vector4f(0.5F, 0.0F, 0.5F, sqrt(2.0f) * 0.5F)

    override fun vertexCount(): Int = 4

    override fun write(vertexList: MutableVertexList) {
        writeVertex(vertexList, 0, 0.0F, 0.0F)
        writeVertex(vertexList, 1, 0.0F, 1.0F)
        writeVertex(vertexList, 2, 1.0F, 1.0F)
        writeVertex(vertexList, 3, 1.0F, 0.0F)
    }

    private fun writeVertex(vertexList: MutableVertexList, i: Int, x: Float, z: Float) = with(vertexList) {
        x(i, x)
        y(i, 0.0F)
        z(i, z)
        r(i, 1.0F)
        g(i, 1.0F)
        b(i, 1.0F)
        u(i, 0.0F)
        v(i, 0.0F)
        light(i, 15728880)
        overlay(i, OverlayTexture.NO_OVERLAY)
        normalX(i, 0.0F)
        normalY(i, 1.0F)
        normalZ(i, 0.0F)
    }

    override fun boundingSphere(): Vector4fc = BOUNDING_SPHERE

    val SHADOW_TEXTURE: ResourceLocation = ResourceLocation.withDefaultNamespace("textures/misc/shadow.png")
    val SHADOW_MATERIAL: SimpleMaterial = SimpleMaterial.builder()
        .texture(SHADOW_TEXTURE)
        .mipmap(false)
        .polygonOffset(true)
        .transparency(Transparency.TRANSLUCENT) // TODO: convert flywheel patch to mixins
        .writeMask(WriteMask.COLOR)
        .build()
    val SHADOW_MODEL = SingleMeshModel(ShadowMesh, SHADOW_MATERIAL)
}