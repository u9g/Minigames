package dev.u9g.minigames.games.gathering.gamemodifiers

import dev.u9g.minigames.games.gathering.itemmodifiers.remakeLore
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.world.LootGenerateEvent

class LootGenerateLoreRemakerListener : Listener {
    @EventHandler
    fun onLootGenerate(event: LootGenerateEvent) {
        event.loot.forEach {
            remakeLore(it)
        }
    }
}