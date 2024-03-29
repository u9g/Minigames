package dev.u9g.minigames.games.listeners

import dev.u9g.minigames.games.Games
import dev.u9g.minigames.games.gathering.itemmodifiers.remakeLore
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.PrepareItemCraftEvent

class MakePickaxeLoreListener : Listener {
    @EventHandler
    fun onPrepareItemToCraft(event: PrepareItemCraftEvent) {
        if (!Games.isPlayerInGame(event.viewers.first())) return

        event.inventory.result?.let { remakeLore(it) }
    }
}