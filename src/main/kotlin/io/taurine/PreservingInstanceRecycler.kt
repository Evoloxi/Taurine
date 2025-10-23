package io.taurine

import dev.engine_room.flywheel.api.instance.Instance

class PreservingInstanceRecycler<I : Instance>(val factory: () -> I) {
    var instances: ArrayList<I> = ArrayList()
    var count: Int = 0

    val size: Int
        get() = instances.size

    fun get(): I {
        val lastCount = count++
        if (lastCount < instances.size) {
            return instances[lastCount]
        } else {
            val out = factory()
            instances.add(out)
            return out
        }
    }

    fun resetCount() {
        count = 0
    }

    fun delete() {
        instances.forEach(Instance::delete)
        instances.clear()
        count = 0
    }

    fun preserve(n: Int) {
        count = minOf(count + n, instances.size)
    }

    fun discardExtra() {
        val size = instances.size
        if (count == size) {
            return
        }

        val extra = instances.subList(count, size)
        extra.forEach(Instance::delete)
        extra.clear()
    }
}