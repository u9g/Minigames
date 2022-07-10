package dev.u9g.minigames.games.listeners

import dev.u9g.minigames.Minigames
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerAdvancementDoneEvent

class HidePlayerAdvancementsListener : Listener {
    @EventHandler
    fun onAdvancementDone(event: PlayerAdvancementDoneEvent) {
        if (event.player !in Minigames.activeGames) return

        // send them the message
        event.message()?.let {
            event.player.sendMessage(it)
        }

        event.message(null)
    }
}