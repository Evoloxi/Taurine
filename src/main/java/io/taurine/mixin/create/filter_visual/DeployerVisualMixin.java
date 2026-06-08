package io.taurine.mixin.create.filter_visual;

import com.simibubi.create.content.kinetics.base.RotatingInstance;
import com.simibubi.create.content.kinetics.deployer.DeployerBlockEntity;
import com.simibubi.create.content.kinetics.deployer.DeployerVisual;
import dev.engine_room.flywheel.api.visual.TickableVisual;
import dev.engine_room.flywheel.api.visualization.VisualizationContext;
import dev.engine_room.flywheel.lib.visual.AbstractVisual;
import io.taurine.visual.impl.FilterVisual;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DeployerVisual.class)
public abstract class DeployerVisualMixin extends AbstractVisual {

    @Unique
    private FilterVisual<DeployerBlockEntity> taurine$filterVisual;

    @Unique
    private RotatingInstance taurine$instance;

    public DeployerVisualMixin(VisualizationContext ctx, Level level, float partialTick) {
        super(ctx, level, partialTick);
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    void init(VisualizationContext context, DeployerBlockEntity blockEntity, float partialTick, CallbackInfo ci) {
        taurine$filterVisual = new FilterVisual<>(context, blockEntity, partialTick);
/*        taurine$instance = instancerProvider().instancer(
                AllInstanceTypes.ROTATING,
                ItemModels.get(level, ((DeployerBlockEntityAccessor) blockEntity).getHeldItem(), ItemDisplayContext.FIXED)
        ).createInstance();
        taurine$instance.light(15);*/
    }

/*    @Inject(method = "<init>", at = @At(value = "INVOKE", target = "Lcom/simibubi/create/content/kinetics/deployer/DeployerVisual;updateRotation(Ldev/engine_room/flywheel/lib/instance/OrientedInstance;Ldev/engine_room/flywheel/lib/instance/OrientedInstance;FFF)V"))
    void updateRot(VisualizationContext context, DeployerBlockEntity be, float partialTick, CallbackInfo ci) {
        Direction facing = be.getBlockState().getValue(FACING);
        taurine$instance.rotation.mul(Axis.YP.rotationDegrees(AngleHelper.horizontalAngle(facing) + 180));
        taurine$instance.setRotationAxis(Direction.Axis.Y).setRotationalSpeed(AngleHelper.horizontalAngle(facing) + 180);

        boolean punching = ((DeployerDuck) be).taurine$isPunching();
        boolean displayMode = facing == Direction.UP && be.getSpeed() == 0 && !punching;

        float xRot = facing == Direction.UP ? 90 : facing == Direction.DOWN ? 270 : 0;
        if (!displayMode) {
            taurine$instance.rotation.mul(Axis.XP.rotationDegrees(xRot));
            taurine$instance.nudge(0, 0, -11 / 16f);
        }
        if (punching) taurine$instance.nudge(0, 1 / 8f, -1 / 16f);


    }

    @Inject(method = "updatePosition", at = @At("TAIL"))
    void updatePos(CallbackInfo ci, @Local(name = "x") float x, @Local(name = "y") float y, @Local(name = "z") float z) {
        taurine$instance.setPosition(x, y, z).setChanged();
    }*/


    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Ldev/engine_room/flywheel/api/instance/Instancer;stealInstance(Ldev/engine_room/flywheel/api/instance/Instance;)V"))
    private void onStealInstance(TickableVisual.Context context, CallbackInfo ci) {
        taurine$filterVisual.update(0f);
    }

    @Inject(method = "_delete", at = @At("TAIL"))
    private void onDelete(CallbackInfo ci) {
        taurine$filterVisual.delete();
    }

    @Inject(method = "updateLight", at = @At("TAIL"))
    private void onUpdateLight(float partialTick, CallbackInfo ci) {
        taurine$filterVisual.updateLight(partialTick);
    }

    @Override
    public void update(float partialTick) {
        taurine$filterVisual.update(partialTick);
    }
}