package io.taurine.mixin.create;

import com.simibubi.create.content.kinetics.deployer.DeployerBlockEntity;
import com.simibubi.create.content.kinetics.deployer.DeployerVisual;
import dev.engine_room.flywheel.api.visual.TickableVisual;
import dev.engine_room.flywheel.api.visualization.VisualizationContext;
import dev.engine_room.flywheel.lib.visual.AbstractVisual;
import io.taurine.visual.FilterVisual;
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

    public DeployerVisualMixin(VisualizationContext ctx, Level level, float partialTick) {
        super(ctx, level, partialTick);
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    void init(VisualizationContext context, DeployerBlockEntity blockEntity, float partialTick, CallbackInfo ci) {
        taurine$filterVisual = new FilterVisual<>(context, blockEntity, partialTick);
    }

    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Ldev/engine_room/flywheel/api/instance/Instancer;stealInstance(Ldev/engine_room/flywheel/api/instance/Instance;)V"))
    private void onUpdate(TickableVisual.Context context, CallbackInfo ci) {
        taurine$filterVisual.update(0f);
    }

    @Inject(method = "_delete", at = @At("TAIL"))
    private void onUpdate(CallbackInfo ci) {
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