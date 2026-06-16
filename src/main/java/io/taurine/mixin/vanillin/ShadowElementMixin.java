package io.taurine.mixin.vanillin;

import dev.engine_room.flywheel.api.instance.InstanceType;
import dev.engine_room.flywheel.lib.instance.ShadowInstance;
import dev.engine_room.flywheel.lib.visual.component.ShadowComponent;
import dev.engine_room.vanillin.elements.ShadowElement;
import io.taurine.flywheel.instance.TaurineInstanceTypes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin({
        ShadowElement.class,
        ShadowComponent.class
})
public class ShadowElementMixin {
    @ModifyArg(
            method = "createInstance",
            at = @At(
                    value = "INVOKE",
                    target = "Ldev/engine_room/flywheel/api/instance/InstancerProvider;instancer(Ldev/engine_room/flywheel/api/instance/InstanceType;Ldev/engine_room/flywheel/api/model/Model;)Ldev/engine_room/flywheel/api/instance/Instancer;"
            )
    )
    private static InstanceType<ShadowInstance> asd(InstanceType<ShadowInstance> type) {
        return TaurineInstanceTypes.INSTANCE.getSHADOW_F16();
    }
}
