package io.taurine.mixin.create.accessor;

import com.simibubi.create.content.kinetics.deployer.DeployerBlockEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(DeployerBlockEntity.class)
public interface DeployerBlockEntityAccessor {
    @Accessor("heldItem")
    ItemStack getHeldItem();

    @Accessor("reach")
    float getReach();
}
