package dev.u9g.minigames.games.listeners

import dev.u9g.minigames.Minigames
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerQuitEvent

class PlayerDisconnectListener : Listener {
    @EventHandler
    fun onDisconnect(event: PlayerQuitEvent) {
        if (event.player !in Minigames.activeGames) return

        Minigames.activeGames[event.player]?.onPlayerLogout()
    }
}