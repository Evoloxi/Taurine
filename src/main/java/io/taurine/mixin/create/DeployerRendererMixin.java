package io.taurine.mixin.create;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.content.kinetics.deployer.DeployerBlockEntity;
import com.simibubi.create.content.kinetics.deployer.DeployerRenderer;
import dev.engine_room.flywheel.api.visualization.VisualizationManager;
import io.taurine.ModelCache;
import io.taurine.mixin.create.accessor.DeployerBlockEntityAccessor;
import net.minecraft.client.renderer.MultiBufferSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(DeployerRenderer.class)
public class DeployerRendererMixin {
    @WrapWithCondition(
            method = "renderSafe(Lcom/simibubi/create/content/kinetics/deployer/DeployerBlockEntity;FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;II)V",
            at = @At(
                value = "INVOKE",
                target = "Lcom/simibubi/create/content/kinetics/deployer/DeployerRenderer;renderItem(Lcom/simibubi/create/content/kinetics/deployer/DeployerBlockEntity;FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;II)V"
            )
    )
    boolean filter(DeployerRenderer instance, DeployerBlockEntity be, float partialTicks, PoseStack ms, MultiBufferSource buffer, int light, int overlay) {
        return !(VisualizationManager.supportsVisualization(be.getLevel()) && ModelCache.canBeInstanced(((DeployerBlockEntityAccessor) be).getHeldItem()));
    }
}
