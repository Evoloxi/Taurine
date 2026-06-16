package io.taurine.flywheel.transform

import io.taurine.extension.Float16
import io.taurine.extension.toHalf

interface IsotropicScale {
    var scale: Float16
}

fun <T : IsotropicScale> T.setScale(s: Float): T = apply {
    scale = s.toHalf()
}