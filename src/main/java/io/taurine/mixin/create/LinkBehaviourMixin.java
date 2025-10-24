package io.taurine.mixin.create;

import com.simibubi.create.content.redstone.link.LinkBehaviour;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import dev.engine_room.flywheel.lib.visualization.VisualizationHelper;
import net.createmod.catnip.platform.CatnipServices;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LinkBehaviour.class)
public abstract class LinkBehaviourMixin extends BlockEntityBehaviour {
    public LinkBehaviourMixin(SmartBlockEntity be) {
        super(be);
    }

    @Inject(method = "read", at = @At("TAIL"))
    private void queueUpdate(CompoundTag nbt, HolderLookup.Provider registries, boolean clientPacket, CallbackInfo ci) {
        CatnipServices.PLATFORM.executeOnClientOnly(() -> () -> VisualizationHelper.queueUpdate(this.blockEntity));
    }
}
