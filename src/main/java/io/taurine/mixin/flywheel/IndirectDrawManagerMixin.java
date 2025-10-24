package io.taurine.mixin.flywheel;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.sugar.Local;
import dev.engine_room.flywheel.api.instance.InstanceType;
import dev.engine_room.flywheel.backend.engine.LightStorage;
import dev.engine_room.flywheel.backend.engine.embed.EnvironmentStorage;
import dev.engine_room.flywheel.backend.engine.indirect.IndirectCullingGroup;
import dev.engine_room.flywheel.backend.engine.indirect.IndirectDrawManager;
import io.taurine.flywheel.patch.IndirectCullingGroupExtension;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Mixin(IndirectDrawManager.class)
public class IndirectDrawManagerMixin {
    @Shadow
    @Final
    private Map<InstanceType<?>, IndirectCullingGroup<?>> cullingGroups;

    @Definition(id = "useOit", local = @Local(type = boolean.class))
    @Expression("useOit")
    @Inject(method = "render", at = @At(value = "MIXINEXTRAS:EXPRESSION"))
    private void taurine$injectSubmitTranslucent(LightStorage lightStorage, EnvironmentStorage environmentStorage, CallbackInfo ci) {
        for (var group : cullingGroups.values()) {
            ((IndirectCullingGroupExtension) group).taurine$submitTranslucent();
        }
    }
}
