package io.taurine.mixin.create;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.content.fluids.tank.FluidTankBlockEntity;
import com.simibubi.create.content.fluids.tank.FluidTankRenderer;
import dev.engine_room.flywheel.api.visualization.VisualizationManager;
import net.minecraft.client.renderer.MultiBufferSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FluidTankRenderer.class)
public class FluidTankRendererMixin {
    @Inject(
            method = "renderSafe(Lcom/simibubi/create/content/fluids/tank/FluidTankBlockEntity;FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;II)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/simibubi/create/content/fluids/tank/FluidTankBlockEntity;getFluidLevel()Lnet/createmod/catnip/animation/LerpedFloat;",
                    ordinal = 0
            ),
            cancellable = true
    )
    private void cancelAfterBoiler(FluidTankBlockEntity be, float partialTicks, PoseStack ms, MultiBufferSource buffer, int light, int overlay, CallbackInfo ci) {
        if (VisualizationManager.supportsVisualization(be.getLevel())) ci.cancel();
    }
}
