package com.luck.picture.lib.config

import java.util.LinkedList

/**
 * @author：luck
 * @date：2023/3/31 4:15 下午
 * @describe：SelectorProviders
 */
class SelectorProviders {
    private val selectionConfigsQueue = LinkedList<SelectorConfig?>()

    fun addSelectorConfigQueue(config: SelectorConfig?) {
        selectionConfigsQueue.add(config)
    }

    val selectorConfig: SelectorConfig?
        get() = if (selectionConfigsQueue.size > 0) selectionConfigsQueue.getLast() else SelectorConfig()

    fun destroy() {
        val selectorConfig = this.selectorConfig
        if (selectorConfig != null) {
            selectorConfig.destroy()
            selectionConfigsQueue.remove(selectorConfig)
        }
    }

    fun reset() {
        for (i in selectionConfigsQueue.indices) {
            val selectorConfig = selectionConfigsQueue.get(i)
            if (selectorConfig != null) {
                selectorConfig.destroy()
            }
        }
        selectionConfigsQueue.clear()
    }

    companion object {
        @Volatile
        private var selectorProviders: SelectorProviders? = null

        val instance: SelectorProviders?
            get() {
                if (SelectorProviders.Companion.selectorProviders == null) {
                    synchronized(SelectorProviders::class.java) {
                        if (SelectorProviders.Companion.selectorProviders == null) {
                            SelectorProviders.Companion.selectorProviders = SelectorProviders()
                        }
                    }
                }
                return SelectorProviders.Companion.selectorProviders
            }
    }
}
