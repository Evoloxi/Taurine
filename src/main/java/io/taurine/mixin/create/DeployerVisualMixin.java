package io.taurine.mixin.create;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.simibubi.create.AllPartialModels;
import com.simibubi.create.content.kinetics.base.RotatingInstance;
import com.simibubi.create.content.kinetics.base.ShaftVisual;
import com.simibubi.create.content.kinetics.deployer.DeployerBlockEntity;
import com.simibubi.create.content.kinetics.deployer.DeployerVisual;
import com.simibubi.create.foundation.render.AllInstanceTypes;
import dev.engine_room.flywheel.api.model.IndexSequence;
import dev.engine_room.flywheel.api.model.Mesh;
import dev.engine_room.flywheel.api.model.Model;
import dev.engine_room.flywheel.api.vertex.MutableVertexList;
import dev.engine_room.flywheel.api.visual.TickableVisual;
import dev.engine_room.flywheel.api.visualization.VisualizationContext;
import dev.engine_room.flywheel.lib.instance.InstanceTypes;
import dev.engine_room.flywheel.lib.instance.TransformedInstance;
import dev.engine_room.flywheel.lib.model.SimpleModel;
import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import dev.engine_room.flywheel.lib.visual.SimpleDynamicVisual;
import dev.engine_room.flywheel.lib.visual.SimpleTickableVisual;
import dev.engine_room.vanillin.item.ItemModels;
import io.taurine.ModelCache;
import io.taurine.duck.DeployerDuck;
import io.taurine.mixin.create.accessor.DeployerBlockEntityAccessor;
import net.createmod.catnip.math.AngleHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.util.Mth;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LightLayer;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.joml.Vector4fc;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

import static com.simibubi.create.content.kinetics.base.DirectionalKineticBlock.FACING;

@Mixin(DeployerVisual.class)
public abstract class DeployerVisualMixin extends ShaftVisual<DeployerBlockEntity> implements SimpleDynamicVisual, SimpleTickableVisual {

    @Unique
    private TransformedInstance taurine$itemInstance;
    @Unique
    private RotatingInstance taurine$rotatingItemInstance;
    @Unique
    private ItemStack taurine$currentHeldItem = ItemStack.EMPTY;
    @Unique
    private boolean taurine$punching;
    @Unique
    private boolean taurine$displayMode;

    @Shadow
    float progress;
    @Shadow
    PartialModel currentHand;

    public DeployerVisualMixin(VisualizationContext ctx, DeployerBlockEntity be, float partialTick) {
        super(ctx, be, partialTick);
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    private void taurine$init(VisualizationContext ctx, DeployerBlockEntity be, float partialTick, CallbackInfo ci) {
        taurine$punching = ((DeployerDuck) be).taurine$isPunching();
        taurine$displayMode = taurine$resolveDisplayMode(be);
        ItemStack heldItem = ((DeployerBlockEntityAccessor) be).getHeldItem();

        if (!heldItem.isEmpty() && ModelCache.canBeInstanced(heldItem)) {
            taurine$currentHeldItem = heldItem.copy();
            if (taurine$displayMode) {
                taurine$createRotatingInstance(heldItem);
            } else {
                taurine$itemInstance = taurine$createTransformedInstance(heldItem);
                taurine$updateTransform();
            }
        }
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void taurine$tick(TickableVisual.Context context, CallbackInfo ci) {
        ItemStack heldItem = ((DeployerBlockEntityAccessor) blockEntity).getHeldItem();
        boolean newPunching = ((DeployerDuck) blockEntity).taurine$isPunching();
        boolean newDisplayMode = taurine$resolveDisplayMode(blockEntity);

        boolean itemChanged = !ItemStack.isSameItemSameComponents(heldItem, taurine$currentHeldItem);
        boolean modeChanged = newPunching != taurine$punching || newDisplayMode != taurine$displayMode;

        if (!itemChanged && !modeChanged) return;

        taurine$punching = newPunching;
        taurine$displayMode = newDisplayMode;
        taurine$currentHeldItem = heldItem.copy();

        taurine$deleteInstances();

        if (!heldItem.isEmpty() && ModelCache.canBeInstanced(heldItem)) {
            if (taurine$displayMode) {
                taurine$createRotatingInstance(heldItem);
            } else {
                taurine$itemInstance = taurine$createTransformedInstance(heldItem);
                taurine$updateTransform();
            }
        }
    }

    @Inject(method = "updatePosition", at = @At("TAIL"))
    private void taurine$onUpdatePosition(CallbackInfo ci) {
        if (!taurine$displayMode) {
            taurine$updateTransform();
        }
    }

    @Inject(method = "updateLight", at = @At("TAIL"))
    private void taurine$updateLight(float partialTick, CallbackInfo ci) {
        int light = LightTexture.pack(
                level.getBrightness(LightLayer.BLOCK, pos),
                level.getBrightness(LightLayer.SKY, pos)
        );
        if (taurine$itemInstance != null) taurine$itemInstance.light(light);
        if (taurine$rotatingItemInstance != null) taurine$rotatingItemInstance.light(light);
    }

    @Inject(method = "_delete", at = @At("TAIL"))
    private void taurine$delete(CallbackInfo ci) {
        taurine$deleteInstances();
    }

    @Unique
    private TransformedInstance taurine$createTransformedInstance(ItemStack stack) {
        ItemDisplayContext ctx = taurine$punching ? ItemDisplayContext.THIRD_PERSON_RIGHT_HAND : ItemDisplayContext.FIXED;

        TransformedInstance inst = instancerProvider().instancer(
                InstanceTypes.TRANSFORMED,
                ItemModels.get(level, stack, ctx)
        ).createInstance();

        inst.light(LightTexture.pack(
                level.getBrightness(LightLayer.BLOCK, pos),
                level.getBrightness(LightLayer.SKY, pos)
        ));

        return inst;
    }

    @Unique
    private final static Vector3f HALF_BLOCK = new Vector3f(0.5f, 0.5f, 0.5f);

    @Unique
    private void taurine$createRotatingInstance(ItemStack stack) {
        BakedModel baked = Minecraft.getInstance().getItemRenderer().getModel(stack, blockEntity.getLevel(), null, 0);

        boolean isBlockItem = (stack.getItem() instanceof BlockItem) && baked.isGui3d();
        float scale = isBlockItem ? 1.25f : 1f;
        float yLift = isBlockItem ? 9 / 16f : 11 / 16f;

        taurine$rotatingItemInstance = instancerProvider().instancer(
                AllInstanceTypes.ROTATING,
                taurine$offset(
                        ItemModels.get(level, stack, ItemDisplayContext.GROUND),
                        HALF_BLOCK,
                        new Vector3f(scale, scale, scale)
                )
        ).createInstance();

        float handLength = taurine$handLength();
        float dist = Math.min(Mth.clamp(progress, 0, 1) * (((DeployerBlockEntityAccessor) blockEntity).getReach() + handLength), 21 / 16f);

        taurine$rotatingItemInstance.setPosition(getVisualPosition())
                .nudge(0, yLift + dist, 0)
                .setRotationOffset(180f)
                .setRotationAxis(Direction.Axis.Y)
                .setRotationalSpeed(20f)
                .light(LightTexture.pack(
                        level.getBrightness(LightLayer.BLOCK, pos),
                        level.getBrightness(LightLayer.SKY, pos)
                ))
                .setChanged();
    }

    @Unique
    private Model taurine$offset(Model model, Vector3f offset, Vector3f scale) {
        List<Model.ConfiguredMesh> meshes = model.meshes().stream().map(mesh ->
                new Model.ConfiguredMesh(mesh.material(), new TransformedMesh(mesh.mesh(), offset, scale))
        ).toList();
        return new SimpleModel(meshes);
    }

    private static class TransformedMesh implements Mesh {
        Mesh original;
        Vector3f offset;
        Vector3f scale;

        private TransformedMesh(Mesh original, Vector3f offset, Vector3f scale) {
            this.original = original;
            this.offset = offset;
            this.scale = scale;
        }

        @Override
        public int vertexCount() {
            return original.vertexCount();
        }

        @Override
        public void write(MutableVertexList vertexList) {
            original.write(vertexList);
            for (int i = 0; i < vertexCount(); i++) {
                vertexList.x(i, vertexList.x(i) * scale.x + offset.x);
                vertexList.y(i, vertexList.y(i) * scale.y + offset.y);
                vertexList.z(i, vertexList.z(i) * scale.z + offset.z);
            }
        }

        @Override
        public IndexSequence indexSequence() {
            return original.indexSequence();
        }

        @Override
        public int indexCount() {
            return original.indexCount();
        }

        @Override
        public Vector4fc boundingSphere() {
            Vector4fc s = original.boundingSphere();
            return new Vector4f(
                    s.x() * scale.x + offset.x,
                    s.y() * scale.y + offset.y,
                    s.z() * scale.z + offset.z,
                    s.w() * Math.max(scale.x, Math.max(scale.y, scale.z)));
        }
    }

    @Unique
    private void taurine$updateTransform() {
        if (taurine$itemInstance == null) return;

        ItemStack heldItem = ((DeployerBlockEntityAccessor) blockEntity).getHeldItem();
        if (heldItem.isEmpty()) return;

        Direction facing = blockState.getValue(FACING);
        Vec3i facingNorm = facing.getNormal();
        BlockPos blockPos = getVisualPosition();

        float handLength = taurine$handLength();
        float dist = Math.min(Mth.clamp(progress, 0, 1) * (((DeployerBlockEntityAccessor) blockEntity).getReach() + handLength), 21 / 16f);

        float baseX = blockPos.getX() + 0.5f;
        float baseY = blockPos.getY() + 0.5f;
        float baseZ = blockPos.getZ() + 0.5f;

        PoseStack ms = new PoseStack();
        ms.translate(baseX + facingNorm.getX() * dist, baseY + facingNorm.getY() * dist, baseZ + facingNorm.getZ() * dist);

        float yRot = AngleHelper.horizontalAngle(facing) + 180;
        float xRot = facing == Direction.UP ? 90 : facing == Direction.DOWN ? 270 : 0;
        ms.mulPose(Axis.YP.rotationDegrees(yRot));
        ms.mulPose(Axis.XP.rotationDegrees(xRot));
        ms.translate(0, 0, -11 / 16f);

        if (taurine$punching) ms.translate(0, 1 / 8f, -1 / 16f);

        BakedModel baked = Minecraft.getInstance().getItemRenderer().getModel(heldItem, blockEntity.getLevel(), null, 0);
        boolean isBlockItem = (heldItem.getItem() instanceof BlockItem) && baked.isGui3d();
        float scale = taurine$punching ? .75f : isBlockItem ? .75f - 1 / 64f : .5f;
        ms.scale(scale, scale, scale);

        taurine$itemInstance.setTransform(ms).setChanged();
    }

    @Unique
    private void taurine$deleteInstances() {
        if (taurine$itemInstance != null) {
            taurine$itemInstance.delete();
            taurine$itemInstance = null;
        }
        if (taurine$rotatingItemInstance != null) {
            taurine$rotatingItemInstance.delete();
            taurine$rotatingItemInstance = null;
        }
    }

    @Unique
    private float taurine$handLength() {
        if (currentHand == AllPartialModels.DEPLOYER_HAND_POINTING) return 0f;
        if (currentHand == AllPartialModels.DEPLOYER_HAND_HOLDING) return 4 / 16f;
        return 3 / 16f;
    }

    @Unique
    private boolean taurine$resolveDisplayMode(DeployerBlockEntity be) {
        return blockState.getValue(FACING) == Direction.UP && be.getSpeed() == 0 && !((DeployerDuck) be).taurine$isPunching();
    }
}
