package io.taurine.mixin.create.accessor;

import com.simibubi.create.content.redstone.link.LinkBehaviour;
import com.simibubi.create.content.redstone.link.RedstoneLinkNetworkHandler;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueBoxTransform;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(LinkBehaviour.class)
public interface LinkBehaviourAccessor {
    @Accessor("firstSlot")
    ValueBoxTransform getFirstSlot();

    @Accessor("secondSlot")
    ValueBoxTransform getSecondSlot();

    @Accessor("frequencyFirst")
    RedstoneLinkNetworkHandler.Frequency getFrequencyFirst();

    @Accessor("frequencyLast")
    RedstoneLinkNetworkHandler.Frequency getFrequencyLast();
}
