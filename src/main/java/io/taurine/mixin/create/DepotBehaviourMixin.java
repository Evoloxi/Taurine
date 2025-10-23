package io.taurine.mixin.create;

import com.simibubi.create.content.logistics.depot.DepotBehaviour;
import com.simibubi.create.content.logistics.depot.DepotBlockEntity;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import dev.engine_room.flywheel.lib.visualization.VisualizationHelper;
import net.createmod.catnip.platform.CatnipClientServices;
import net.createmod.catnip.platform.CatnipServices;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DepotBehaviour.class)
public abstract class DepotBehaviourMixin extends BlockEntityBehaviour {
    public DepotBehaviourMixin(SmartBlockEntity be) {
        super(be);
    }

    @Inject(method = "read", at = @At("TAIL"))
    void asd(CompoundTag compound, HolderLookup.Provider registries, boolean clientPacket, CallbackInfo ci) {
        if (blockEntity instanceof DepotBlockEntity depot) {
            CatnipServices.PLATFORM.executeOnClientOnly(() -> () -> VisualizationHelper.queueUpdate(depot));
        }
    }

    /*@Inject(
            method = "tick(Lcom/simibubi/create/content/kinetics/belt/transport/TransportedItemStack;)Z",
            at = @At(
                    value = "FIELD",
                    target = "Lcom/simibubi/create/content/kinetics/belt/transport/TransportedItemStack;beltPosition:F",
                    ordinal = 2
            )
    )
    void tickItemPositions(TransportedItemStack heldItem, CallbackInfoReturnable<Boolean> cir) {
        if (blockEntity instanceof DepotBlockEntity depot) {
            VisualizationHelper.queueUpdate(depot);
        }
    }*/
}
