package io.taurine.mixin.create.accessor;

import com.simibubi.create.foundation.blockEntity.behaviour.ValueBoxRenderer;
import net.minecraft.world.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ValueBoxRenderer.class)
public interface ValueBoxRendererAccessor {
    @Invoker("customZOffset")
    float invokeCustomZOffset(Item item);
}
