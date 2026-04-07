package io.taurine.mixin.create;

import com.llamalad7.mixinextras.sugar.Local;
import com.simibubi.create.content.kinetics.belt.BeltBlockEntity;
import com.simibubi.create.content.kinetics.belt.BeltSlicer;
import dev.engine_room.flywheel.lib.visualization.VisualizationHelper;
import net.createmod.catnip.platform.CatnipServices;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BeltSlicer.class)
public abstract class BeltSlicerMixin {

    /*@Inject(method = "useConnector", at = @At(value = "INVOKE", target = "Lcom/simibubi/create/content/kinetics/belt/BeltBlockEntity;detachKinetics()V"))
    private static void queueUpdate(BlockState state, Level world, BlockPos pos, Player player, InteractionHand handIn, BlockHitResult hit, BeltSlicer.Feedback feedBack, CallbackInfoReturnable<ItemInteractionResult> cir, @Local(name = "belt") BeltBlockEntity belt) {
        CatnipServices.PLATFORM.executeOnClientOnly(() -> () -> VisualizationHelper.queueUpdate(belt));
    }*/
}
