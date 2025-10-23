package io.taurine.mixin.create.accessor;

import com.simibubi.create.content.kinetics.belt.transport.TransportedItemStack;
import com.simibubi.create.content.logistics.depot.DepotBehaviour;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(DepotBehaviour.class)
public interface DepotBehaviourAccessor {
    @Accessor("incoming")
    List<TransportedItemStack> getIncoming();

    @Accessor("heldItem")
    TransportedItemStack getHeldItem();

    @Accessor("processingOutputBuffer")
    ItemStackHandler getProcessingOutputBuffer();
}
