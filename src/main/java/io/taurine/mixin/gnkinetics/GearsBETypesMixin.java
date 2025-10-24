package io.taurine.mixin.gnkinetics;

import com.llamalad7.mixinextras.injector.ModifyReceiver;
import com.simibubi.create.AllBlockEntityTypes;
import com.simibubi.create.content.kinetics.belt.BeltBlockEntity;
import com.simibubi.create.content.kinetics.simpleRelays.BracketedKineticBlockEntity;
import com.simibubi.create.foundation.data.CreateBlockEntityBuilder;
import com.simibubi.create.foundation.data.CreateRegistrate;
import com.tterrag.registrate.util.nullness.NonNullSupplier;
import dev.engine_room.flywheel.lib.visualization.SimpleBlockEntityVisualizer;
import dev.lopyluna.gnkinetics.content.blocks.kinetics.chainned_cog.ChainableCogwheelBE;
import dev.lopyluna.gnkinetics.register.GearsBETypes;
import io.taurine.visual.ChainableCogwheelVisual;
import io.taurine.visual.OmniCogwheelVisual;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Slice;

@Mixin(GearsBETypes.class)
public class GearsBETypesMixin {
    @ModifyReceiver(
            method = "<clinit>",
            slice = @Slice(
                    from = @At(value = "CONSTANT", args = "stringValue=cogwheel")
            ),
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/simibubi/create/foundation/data/CreateBlockEntityBuilder;validBlocks([Lcom/tterrag/registrate/util/nullness/NonNullSupplier;)Lcom/tterrag/registrate/builders/BlockEntityBuilder;",
                    ordinal = 0
            )
    )
    private static <T extends BracketedKineticBlockEntity> CreateBlockEntityBuilder<T, CreateRegistrate> addVisualToCogwheels(
            CreateBlockEntityBuilder<T, CreateRegistrate> instance,
            NonNullSupplier<T>[] nonNullSuppliers
    ) {
        return instance.visual(() -> OmniCogwheelVisual::create, false);
    }

    @ModifyReceiver(
            method = "<clinit>",
            slice = @Slice(
                    from = @At(value = "CONSTANT", args = "stringValue=chainable_cogwheel")
            ),
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/simibubi/create/foundation/data/CreateBlockEntityBuilder;validBlocks([Lcom/tterrag/registrate/util/nullness/NonNullSupplier;)Lcom/tterrag/registrate/builders/BlockEntityBuilder;",
                    ordinal = 0
            )
    )
    private static <T extends ChainableCogwheelBE> CreateBlockEntityBuilder<T, CreateRegistrate> addVisualToChainableCogwheel(
            CreateBlockEntityBuilder<T, CreateRegistrate> instance,
            NonNullSupplier<T>[] nonNullSuppliers
    ) {
        return instance.visual(() -> ChainableCogwheelVisual::new, true);
    }

    @ModifyArg(
            method = "<clinit>",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/simibubi/create/foundation/data/CreateBlockEntityBuilder;visual(Lcom/tterrag/registrate/util/nullness/NonNullSupplier;Z)Lcom/simibubi/create/foundation/data/CreateBlockEntityBuilder;"
            ),
            index = 1
    )
    private static boolean disableRenderSafe(boolean renderNormally) {
        return false;
    }
}

