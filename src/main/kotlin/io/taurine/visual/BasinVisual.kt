package io.taurine.visual

import com.mojang.blaze3d.vertex.PoseStack
import com.simibubi.create.content.fluids.FluidMesh
import com.simibubi.create.content.processing.basin.BasinBlock
import com.simibubi.create.content.processing.basin.BasinBlockEntity
import com.simibubi.create.foundation.blockEntity.behaviour.fluid.SmartFluidTankBehaviour
import dev.engine_room.flywheel.api.visual.DynamicVisual
import dev.engine_room.flywheel.api.visualization.VisualizationContext
import dev.engine_room.flywheel.lib.transform.TransformStack
import dev.engine_room.flywheel.lib.visual.SimpleDynamicVisual
import io.taurine.ModelCache.hashItem
import io.taurine.SmartPreservingRecycler
import io.taurine.extension.inaccessible.ingredientRotation
import io.taurine.extension.inaccessible.itemCapability
import io.taurine.extension.inaccessible.visualizedOutputItems
import io.taurine.extension.pixel
import io.taurine.extension.translate
import net.createmod.catnip.animation.AnimationTickHolder
import net.createmod.catnip.math.AngleHelper
import net.createmod.catnip.math.VecHelper
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.LightTexture
import net.minecraft.client.renderer.texture.TextureAtlasSprite
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.util.Mth
import net.minecraft.util.RandomSource
import net.minecraft.world.inventory.InventoryMenu
import net.minecraft.world.item.ItemDisplayContext
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.LightLayer
import net.minecraft.world.phys.Vec3
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions
import net.neoforged.neoforge.items.IItemHandlerModifiable
import net.neoforged.neoforge.items.ItemStackHandler
import kotlin.math.max

class BasinVisual(
    visualizationContext: VisualizationContext, be: BasinBlockEntity, delta: Float
) : ItemRenderingBlockEntityVisual<BasinBlockEntity>(
    visualizationContext, be, delta
), SimpleDynamicVisual {

    override val itemDisplayContext = ItemDisplayContext.GROUND

    val filters = FilterVisual<BasinBlockEntity>(visualizationContext, blockEntity, delta)

    val fluidInstances = SmartPreservingRecycler<TextureAtlasSprite, ScalingFluidInstance> {
        visualizationContext.instancerProvider().instancer(
            TaurineInstanceTypes.SCALING_FLUID,
            FluidMesh.surface(it, 12f / 16f)
        ).createInstance()
    }

    var fluidLevel = 0f

    fun updateInstances(partialTicks: Float, ms: PoseStack) {
        /* TODO: fluid rendering
            val fluidLevel: Float = renderFluids(basin, partialTicks, ms, buffer, light, overlay)
        */
        //val level = Mth.clamp(fluidLevel - .3f, .125f, .6f)
        val level = Mth.clamp(fluidLevel - .3f, .125f, .6f)

        ms.pushPose()

        ms.translate(.5, .2, .5)
        TransformStack.of(ms).rotateYDegrees(blockEntity.ingredientRotation.getValue(partialTicks))

        val r = RandomSource.create(pos.hashCode().toLong())
        var baseVector = Vec3(.125, level.toDouble(), 0.0)

        val inv: IItemHandlerModifiable = blockEntity.itemCapability ?: ItemStackHandler()
        var itemCount = 0
        for (slot in 0..<inv.slots) if (!inv.getStackInSlot(slot).isEmpty) itemCount++

        if (itemCount == 1) baseVector = Vec3(0.0, level.toDouble(), 0.0)

        val anglePartition = 360f / itemCount
        for (slot in 0..<inv.slots) {
            val stack = inv.getStackInSlot(slot)
            if (stack.isEmpty) continue

            ms.pushPose()

            if (fluidLevel > 0) {
                ms.translate(
                    0f, (Mth.sin(
                        AnimationTickHolder.getRenderTime(this.level) / 12f + anglePartition * itemCount
                    ) + 1.5f) * 1 / 32f, 0f
                )
            }

            val itemPosition = VecHelper.rotate(baseVector, (anglePartition * itemCount).toDouble(), Direction.Axis.Y)
            ms.translate(itemPosition.x, itemPosition.y, itemPosition.z)
            TransformStack.of(ms).rotateYDegrees(anglePartition * itemCount + 35).rotateXDegrees(65f)

            for (i in 0..stack.count / 8) {
                ms.pushPose()

                val vec = VecHelper.offsetRandomly(Vec3.ZERO, r, 1 / 16f)

                ms.translate(vec.x, vec.y, vec.z)
                renderItem(ms,stack)
                ms.popPose()
            }
            ms.popPose()

            itemCount--
        }
        ms.popPose()

        val blockState = blockEntity.blockState
        if (blockState.block !is BasinBlock) return
        val direction = blockState.getValue(BasinBlock.FACING)
        if (direction == Direction.DOWN) return
        val directionVec = Vec3.atLowerCornerOf(direction.normal)
        val outVec = VecHelper.getCenterOf(BlockPos.ZERO).add(
                directionVec.scale(.55).subtract(0.0, (1 / 2f).toDouble(), 0.0)
            )

        val outToBasin = this.level.getBlockState(
            pos.relative(direction)
            ).block is BasinBlock

        for (intAttached in blockEntity.visualizedOutputItems) {
            val progress = 1 - (intAttached.getFirst() - partialTicks) / BasinBlockEntity.OUTPUT_ANIMATION_TIME

            if (!outToBasin && progress < .35f) continue

            ms.pushPose()
            TransformStack.of(ms).translate(outVec).translate(Vec3(0.0, max(-.55f, -(progress * progress * 2)).toDouble(), 0.0))
                .translate(directionVec.scale((progress * .5f).toDouble())).rotateYDegrees(AngleHelper.horizontalAngle(direction))
                .rotateXDegrees(progress * 180)
            renderItem(ms, intAttached.getValue())
            ms.popPose()
        }
    }

    fun renderItem(ms: PoseStack, item: ItemStack) {
        val key = hashItem(item, itemDisplayContext)
        loadModel(item, key) ?: return
        instances.get(key).apply {
            setIdentityTransform()
            setTransform(ms)
            setChanged()
        }
    }
    override fun update(partialTick: Float) {
        instances.resetCount()
        val ms = PoseStack().apply {
            translate(visualPosition)
        }
        updateInstances(partialTick, ms)
        updateLight(partialTick)
        instances.discardExtra()
        filters.update(partialTick)
        fluidLevel = rebuildFluids(partialTick, ms)
    }

    @Suppress("UnnecessaryVariable")
    fun rebuildFluids(partialTick: Float, ms: PoseStack): Float {
        val inputFluids = blockEntity.getBehaviour(SmartFluidTankBehaviour.INPUT)
        val outputFluids = blockEntity.getBehaviour(SmartFluidTankBehaviour.OUTPUT)
        val tanks = arrayOf(inputFluids, outputFluids)
        val totalUnits = blockEntity.getTotalFluidUnits(partialTick)

        if (totalUnits < 1) {
            fluidInstances.resetCount()
            fluidInstances.discardExtra()
            return 0f
        }

        fluidInstances.resetCount()

        val tankInset = 2.pixel
        val tankWidth = 12.pixel
        val tankHeightBase = 12.pixel
        val tankTopOffset = 14.pixel
        val horizontalScale = (16f / 12f) / 2f

        var fluidLevel = Mth.clamp(totalUnits / 2000f, 0f, 1f)
        fluidLevel = 1 - ((1 - fluidLevel) * (1 - fluidLevel))

        var xMin = tankInset
        var xMax = tankInset
        val yMin = tankInset
        val yMax = yMin + tankHeightBase * fluidLevel
        val zMin = tankInset
        val zMax = tankTopOffset

        for (behaviour in tanks) {
            for (tankSegment in behaviour.getTanks()) {
                val renderedFluid = tankSegment.getRenderedFluid()
                if (renderedFluid.isEmpty) continue

                val units = tankSegment.getTotalUnits(partialTick)
                if (units < 1) continue

                val clientFluid = IClientFluidTypeExtensions.of(renderedFluid.fluid)
                val atlas = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS)
                val sprite = atlas.apply(clientFluid.getStillTexture(renderedFluid))

                val portion = Mth.clamp(units / totalUnits, 0f, 1f)

                val fluidWidth = portion * tankWidth
                xMax += fluidWidth

                val centerX = (xMin + xMax) / 2.0f
                val centerZ = (zMin + zMax) / 2.0f

                val scaleX = (xMax - xMin) * horizontalScale
                val scaleZ = (zMax - zMin) * horizontalScale

                ms.pushPose()
                TransformStack.of(ms)
                    .translate(centerX, yMax, centerZ)
                    .scale(scaleX, 1f, scaleZ)
                    .rotateTo(Direction.NORTH, Direction.EAST)

                val tint = clientFluid.getTintColor(renderedFluid.fluid.defaultFluidState(), level, pos)

                // TODO: vertical fluid separation plane

                fluidInstances.get(sprite).apply {
                    setIdentityTransform()
                    setTransform(ms)

                    val uRange = sprite.u1 - sprite.u0
                    val vRange = sprite.v1 - sprite.v0
                    u0 = sprite.u0 + uRange * 0.5f
                    v0 = sprite.v0 + vRange * 0.5f
                    uScale = uRange * scaleX
                    vScale = vRange * 0.5f

                    colorArgb(tint)
                    setChanged()
                }

                ms.popPose()
                xMin = xMax
            }
        }
        fluidInstances.discardExtra()
        updateLight(partialTick)
        return yMax
    }
    override fun _delete() {
        instances.delete()
        filters.delete()
        fluidInstances.delete()
    }

    init {
        update(delta)
    }

    override fun updateLight(partialTick: Float) { // don't really gaf
        super.updateLight(partialTick)
        filters.updateLight(partialTick)
        val packed = LightTexture.pack(
            level.getBrightness(LightLayer.BLOCK, pos),
            level.getBrightness(LightLayer.SKY, pos)
        )
        fluidInstances.applyToAll {
            light = packed
            setChanged()
        }
    }

    override fun beginFrame(ctx: DynamicVisual.Context) {
        rebuildFluids(ctx.partialTick(), PoseStack().apply { translate(visualPosition) })
    }
}