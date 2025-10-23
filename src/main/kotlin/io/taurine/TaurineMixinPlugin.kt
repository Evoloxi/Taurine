package io.taurine

import me.fallenbreath.conditionalmixin.api.mixin.RestrictiveMixinConfigPlugin

class TaurineMixinPlugin : RestrictiveMixinConfigPlugin() {
    override fun getRefMapperConfig(): String? = null

    override fun acceptTargets(myTargets: Set<String>, otherTargets: Set<String>) = Unit

    override fun getMixins(): List<String> {
        return emptyList()
    }
}