package io.taurine.mixin.vanillin.accesor;

import dev.engine_room.vanillin.config.BlockEntityVisualizerBuilder;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(BlockEntityVisualizerBuilder.class)
public interface BlockEntityVisualizerBuilderAccessor<T extends BlockEntity> {
    @Accessor("type")
    BlockEntityType<T> accessor$type();
}
