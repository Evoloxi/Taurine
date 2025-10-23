package io.taurine.mixin.create.accessor;

import com.simibubi.create.content.logistics.depot.DepotBehaviour;
import com.simibubi.create.content.logistics.depot.DepotBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(DepotBlockEntity.class)
public interface DepotBlockEntityAccessor {
    @Accessor("depotBehaviour")
    DepotBehaviour getDepotBehaviour();
}

