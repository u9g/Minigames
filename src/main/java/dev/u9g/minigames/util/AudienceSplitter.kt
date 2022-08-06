package dev.u9g.minigames.util

import net.kyori.adventure.audience.Audience
import java.util.function.Predicate

data class SplitAudience(val wanted: Audience, val unwanted: Audience) {
    companion object {
        fun splitAudience(audience: Audience, splitter: Predicate<in Audience>): SplitAudience {
            return SplitAudience(
                    audience.filterAudience(splitter),
                    audience.filterAudience { !splitter.test(it) }
            )
        }
    }
}