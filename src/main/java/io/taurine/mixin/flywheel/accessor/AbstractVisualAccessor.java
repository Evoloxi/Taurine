package io.taurine.mixin.flywheel.accessor;

import dev.engine_room.flywheel.api.instance.InstancerProvider;
import dev.engine_room.flywheel.lib.visual.AbstractVisual;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(value = AbstractVisual.class, remap = false)
public interface AbstractVisualAccessor {
    @Invoker("instancerProvider")
    InstancerProvider getInstancerProvider();

    @Accessor("level")
    Level getLevel();
}
