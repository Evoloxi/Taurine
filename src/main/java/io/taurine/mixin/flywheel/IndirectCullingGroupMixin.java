package io.taurine.mixin.flywheel;

import com.llamalad7.mixinextras.injector.ModifyReceiver;
import com.llamalad7.mixinextras.sugar.Local;
import dev.engine_room.flywheel.api.instance.Instance;
import dev.engine_room.flywheel.api.instance.InstanceType;
import dev.engine_room.flywheel.api.material.Transparency;
import dev.engine_room.flywheel.backend.compile.ContextShader;
import dev.engine_room.flywheel.backend.compile.IndirectPrograms;
import dev.engine_room.flywheel.backend.compile.PipelineCompiler;
import dev.engine_room.flywheel.backend.engine.MaterialRenderState;
import dev.engine_room.flywheel.backend.engine.indirect.IndirectBuffers;
import dev.engine_room.flywheel.backend.engine.indirect.IndirectCullingGroup;
import dev.engine_room.flywheel.backend.engine.indirect.IndirectDraw;
import dev.engine_room.flywheel.backend.gl.shader.GlProgram;
import io.taurine.flywheel.patch.IndirectCullingGroupExtension;
import io.taurine.flywheel.patch.MultiDraw;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

@Mixin(IndirectCullingGroup.class)
public abstract class IndirectCullingGroupMixin<I extends Instance> implements IndirectCullingGroupExtension {
    @Shadow
    @Final
    private IndirectPrograms programs;
    @Shadow
    @Final
    private InstanceType<I> instanceType;

    @Shadow
    protected abstract void drawBarrier();

    @Shadow
    @Final
    private IndirectBuffers buffers;
    @Shadow
    @Final
    private List<MultiDraw> multiDraws;
    @Shadow
    @Final
    private List<MultiDraw> oitDraws;
    @Unique
    private final List<MultiDraw> taurine$translucentDraws = new ArrayList<>();

    @Override
    public void taurine$submitTranslucent() {
        if (!this.taurine$translucentDraws.isEmpty()) {
            this.buffers.bindForDraw();
            this.drawBarrier();
            GlProgram lastProgram = null;

            for(MultiDraw multiDraw : this.taurine$translucentDraws) {
                GlProgram drawProgram = this.programs.getIndirectProgram(this.instanceType,
                        multiDraw.embedded ? ContextShader.EMBEDDED : ContextShader.DEFAULT,
                        multiDraw.material, PipelineCompiler.OitMode.OFF);
                if (drawProgram != lastProgram) {
                    lastProgram = drawProgram;
                    drawProgram.bind();
                }

                MaterialRenderState.setup(multiDraw.material);
                multiDraw.submit(drawProgram);
            }
        }
    }

    @Inject(method = "sortDraws", at = @At("HEAD"))
    private void taurine$clearTranslucentDraws(CallbackInfo ci) {
        this.taurine$translucentDraws.clear();
    }

    @ModifyReceiver(
            method = "sortDraws",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/util/List;add(Ljava/lang/Object;)Z"
            )
    )
    private <E> List<MultiDraw> taurine$filterTranslucentDraws(List<MultiDraw> original, E e, @Local(name = "draw1") IndirectDraw draw1) {
        Transparency transparency = draw1.material().transparency();
        List<MultiDraw> dst;

        if (transparency == Transparency.ORDER_INDEPENDENT) {
            dst = this.oitDraws;
        } else if (transparency == Transparency.OPAQUE) {
            dst = this.multiDraws;
        } else {
            dst = this.taurine$translucentDraws;
        }
        return dst;
    }
}
