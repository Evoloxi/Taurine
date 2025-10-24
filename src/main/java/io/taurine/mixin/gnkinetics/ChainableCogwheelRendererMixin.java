package io.taurine.mixin.gnkinetics;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.content.kinetics.chainConveyor.ChainConveyorBlockEntity;
import dev.engine_room.flywheel.api.visualization.VisualizationManager;
import dev.lopyluna.gnkinetics.content.blocks.kinetics.chainned_cog.ChainableCogwheelBE;
import dev.lopyluna.gnkinetics.content.blocks.kinetics.chainned_cog.ChainableCogwheelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.Direction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChainableCogwheelRenderer.class)
public abstract class ChainableCogwheelRendererMixin {
    @Shadow
    protected abstract void renderChains(ChainableCogwheelBE be, Direction.Axis axis, PoseStack ms, MultiBufferSource buffer, int light, int overlay);

    @Definition(id = "fromAxisAndDirection", method = "Lnet/minecraft/core/Direction;fromAxisAndDirection(Lnet/minecraft/core/Direction$Axis;Lnet/minecraft/core/Direction$AxisDirection;)Lnet/minecraft/core/Direction;")
    @Expression("? = fromAxisAndDirection(?, ?)")
    @Inject(
            method = "renderSafe(Ldev/lopyluna/gnkinetics/content/blocks/kinetics/chainned_cog/ChainableCogwheelBE;FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;II)V",
            at = @At(
                    value = "MIXINEXTRAS:EXPRESSION", 
                    shift = At.Shift.AFTER
            ),
            cancellable = true
    )
    private void renderSafe(
            ChainableCogwheelBE be,
            float partialTicks,
            PoseStack ms,
            MultiBufferSource buffer,
            int light,
            int overlay,
            CallbackInfo ci,
            @Local(name = "axis") Direction.Axis axis,
            @Local(name = "facing") Direction facing
    ) {
        if (VisualizationManager.supportsVisualization(be.getLevel())) {
            renderChains(be, axis, ms, buffer, light, overlay); // TODO: figure out whether instancing chains is feasible
            ci.cancel();
        }
    }
}