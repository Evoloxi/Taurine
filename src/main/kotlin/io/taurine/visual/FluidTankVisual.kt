package io.taurine.visual

import com.mojang.blaze3d.vertex.PoseStack
import com.simibubi.create.content.fluids.FluidMesh
import com.simibubi.create.content.fluids.tank.FluidTankBlockEntity
import dev.engine_room.flywheel.api.instance.Instance
import dev.engine_room.flywheel.api.material.CardinalLightingMode
import dev.engine_room.flywheel.api.material.Transparency
import dev.engine_room.flywheel.api.model.Model
import dev.engine_room.flywheel.api.vertex.MutableVertexList
import dev.engine_room.flywheel.api.visual.DynamicVisual
import dev.engine_room.flywheel.api.visualization.VisualizationContext
import dev.engine_room.flywheel.lib.material.SimpleMaterial
import dev.engine_room.flywheel.lib.model.QuadMesh
import dev.engine_room.flywheel.lib.model.SingleMeshModel
import dev.engine_room.flywheel.lib.visual.AbstractBlockEntityVisual
import dev.engine_room.flywheel.lib.visual.SimpleDynamicVisual
import io.taurine.extension.inaccessible.window
import io.taurine.extension.translate
import io.taurine.flywheel.ScalingFluidInstance
import io.taurine.flywheel.SmartPreservingRecycler
import io.taurine.flywheel.TaurineInstanceTypes
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.LightTexture
import net.minecraft.client.renderer.texture.OverlayTexture
import net.minecraft.client.renderer.texture.TextureAtlasSprite
import net.minecraft.core.Direction
import net.minecraft.util.Mth
import net.minecraft.world.inventory.InventoryMenu
import net.minecraft.world.level.LightLayer
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions
import org.joml.Vector4f
import org.joml.Vector4fc
import java.util.function.Consumer
import kotlin.math.min

@JvmRecord
data class FaceData(
    val normal: FloatArray,
    val getPos: (x: Float, y: Float, idx: Int) -> FloatArray
)

object FluidTankMesh {
    /*private val CUBE: RendererReloadCache<SurfaceKey, Model> = RendererReloadCache { sprite: SurfaceKey ->
        SingleMeshModel(TexturedCubeMesh(sprite.texture, 1f), material(sprite.texture))
    }*/

    data class TexturedCubeMesh(val texture: TextureAtlasSprite, val sides: Collection<Direction>) : QuadMesh {
        override fun vertexCount(): Int = 6 * 4

        override fun write(vertexList: MutableVertexList) {
            TODO()
        }
        override fun boundingSphere(): Vector4fc {
            return Vector4f(0f, 0f, 0f, 1f / Mth.SQRT_OF_TWO)
        }
    }


    fun tube(sprite: TextureAtlasSprite): Model {
        return SingleMeshModel(
            TexturedCubeMesh(
                sprite,
                listOf(
                    Direction.NORTH,
                    Direction.SOUTH,
                    Direction.EAST,
                    Direction.WEST,
                    Direction.DOWN,
                    Direction.UP,
                )
            ), material(sprite)
        )
    }

    private fun material(sprite: TextureAtlasSprite): SimpleMaterial {
        return SimpleMaterial.builder()
            .cardinalLightingMode(CardinalLightingMode.OFF)
            .texture(sprite.atlasLocation())
            .transparency(Transparency.ORDER_INDEPENDENT)
            .build()
    }

    @JvmRecord
    private data class SurfaceKey(val texture: TextureAtlasSprite, val width: Float)
}

private fun Boolean.toFloat(): Float {
    return if (this) 1f else 0f
}

class FluidTankVisual<T: FluidTankBlockEntity>(
    visualizationContext: VisualizationContext,
    be: T,
    delta: Float
) : AbstractBlockEntityVisual<T>(
visualizationContext, be, delta
), SimpleDynamicVisual {

    val sides = SmartPreservingRecycler<TextureAtlasSprite, ScalingFluidInstance> {
        visualizationContext.instancerProvider().instancer(
            TaurineInstanceTypes.SCALING_FLUID,
            FluidTankMesh.tube(it)
        ).createInstance()
    }
    val top = SmartPreservingRecycler<TextureAtlasSprite, ScalingFluidInstance> {
        visualizationContext.instancerProvider().instancer(
            TaurineInstanceTypes.SCALING_FLUID,
            FluidMesh.surface(it, 1f)
        ).createInstance()
    }

    override fun _delete() {
        sides.delete()
    }

    fun renderSafe(
        be: FluidTankBlockEntity,
        partialTicks: Float,
        ms: PoseStack
    ) {
        if (!be.window) return

        val fluidLevel = be.fluidLevel ?: return

        val capHeight = 1 / 4f
        val tankHullWidth = 1 / 16f + 1 / 128f
        val minPuddleHeight = 1 / 16f
        val totalHeight = be.height - 2 * capHeight - minPuddleHeight

        val level = fluidLevel.getValue(partialTicks)
        if (level < 1 / (512f * totalHeight)) return
        val clampedLevel = Mth.clamp(level * totalHeight, 0f, totalHeight)

        val tank = be.tankInventory
        val fluidStack = tank.getFluid()

        if (fluidStack.isEmpty) return

        val top = fluidStack.fluid
            .fluidType
            .isLighterThanAir

        val xMin = tankHullWidth
        xMin + be.width - 2 * tankHullWidth
        var yMin = totalHeight + capHeight + minPuddleHeight - clampedLevel
        var yMax = yMin + clampedLevel

        if (top) {
            yMin += totalHeight - clampedLevel
            yMax += totalHeight - clampedLevel
        }

        val zMin = tankHullWidth
        zMin + be.width - 2 * tankHullWidth

        ms.pushPose()
        ms.translate(0.5f, clampedLevel, 0.5f)
        ms.scale(
            be.width - 2 * tankHullWidth,
            clampedLevel,
            be.width - 2 * tankHullWidth
        )
        val clientFluid = IClientFluidTypeExtensions.of(fluidStack.fluid)
        val atlas = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS)
        val sprite = atlas.apply(clientFluid.getStillTexture(fluidStack))
        val tint = clientFluid.getTintColor(fluidStack.fluid.defaultFluidState(), this.level, pos)
        val light = LightTexture.pack(
            this.level.getBrightness(LightLayer.BLOCK, pos),
            this.level.getBrightness(LightLayer.SKY, pos)
        )
        sides.get(sprite).apply {
            setIdentityTransform()
            setTransform(ms)

            val uRange = sprite.u1 - sprite.u0
            val vRange = sprite.v1 - sprite.v0
            u0 = sprite.u0
            v0 = sprite.v0
            uScale = uRange * 10f
            vScale = vRange * 10f
            light(light)
            colorArgb(tint)
            setChanged()
        }

        ms.popPose()
    }

    override fun update(partialTick: Float) {
        if (!blockEntity.isController) return

        sides.resetCount()

        val ms = PoseStack().apply {
            translate(visualPos)
        }
        renderSafe(blockEntity, partialTick, ms)
        updateLight(partialTick)
        sides.discardExtra()

    }

    override fun collectCrumblingInstances(consumer: Consumer<Instance?>) = Unit

    override fun updateLight(partialTick: Float) {
        val packed = LightTexture.pack(
            level.getBrightness(LightLayer.BLOCK, pos),
            level.getBrightness(LightLayer.SKY, pos)
        )
        sides.applyToAll {
            light = packed
            setChanged()
        }
    }

    override fun beginFrame(ctx: DynamicVisual.Context) {
        update(ctx.partialTick())
    }
}