package io.architekton.flywheel

import dev.engine_room.flywheel.api.instance.Instance
import io.taurine.PreservingInstanceRecycler

class SmartPreservingRecycler<K, I : Instance>(val factory: (K) -> I) {
    val recyclers: MutableMap<K, PreservingInstanceRecycler<I>> = mutableMapOf()

    fun get(key: K): I {
        return recyclers.computeIfAbsent(key) {
            PreservingInstanceRecycler { factory(key) }
        }.get()
    }

    fun resetCount() {
        recyclers.values.forEach(PreservingInstanceRecycler<I>::resetCount)
    }

    fun delete() {
        recyclers.values.forEach(PreservingInstanceRecycler<I>::delete)
        recyclers.clear()
    }

    fun discardExtra() {
        recyclers.values.forEach(PreservingInstanceRecycler<I>::discardExtra)
    }

    fun preserve(key: K, n: Int) {
        recyclers.computeIfAbsent(key) {
            PreservingInstanceRecycler { factory(key) }
        }.preserve(n)
    }

    inline fun applyToAll(crossinline block: I.() -> Unit) {
        for (recycler in recyclers.values) {
            for (it in recycler.instances) {
                it.block()
            }
        }
    }
}