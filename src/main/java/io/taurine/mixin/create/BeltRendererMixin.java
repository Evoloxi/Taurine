package io.taurine.mixin.create;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.content.kinetics.belt.BeltBlockEntity;
import com.simibubi.create.content.kinetics.belt.BeltRenderer;
import com.simibubi.create.content.kinetics.belt.BeltSlope;
import com.simibubi.create.content.kinetics.belt.transport.TransportedItemStack;
import dev.engine_room.flywheel.api.visualization.VisualizationManager;
import io.taurine.ModelCache;
import io.taurine.extension.ItemStackExtensionKt;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(BeltRenderer.class)
public class BeltRendererMixin {

    @ModifyReturnValue(
            method = "shouldRenderOffScreen*",
            at = @At("RETURN")
    )
    private boolean modifyShouldRenderOffScreen(boolean original) {
        return true;
    }

    @WrapWithCondition(
            method = "renderItems",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/simibubi/create/content/kinetics/belt/BeltRenderer;renderItem(Lcom/simibubi/create/content/kinetics/belt/BeltBlockEntity;FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;IILnet/minecraft/core/Direction;Lnet/minecraft/core/Vec3i;Lcom/simibubi/create/content/kinetics/belt/BeltSlope;IZZLcom/simibubi/create/content/kinetics/belt/transport/TransportedItemStack;Lnet/minecraft/world/phys/Vec3;)V"
            ),
            require = 2
    )
    boolean filter(BeltRenderer instance, BeltBlockEntity be, float positionVec, PoseStack vectorForOffset, MultiBufferSource diff, int yRot, int renderViewEntity, Direction box, Vec3i v, BeltSlope beltSlope, int i, boolean partialTicks, boolean ms, TransportedItemStack itemStack, Vec3 light) {
        return !(VisualizationManager.supportsVisualization(be.getLevel()) && ItemStackExtensionKt.canBeInstanced(itemStack.stack));
    }
}

