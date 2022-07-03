package dev.u9g.minigames.games

import dev.u9g.minigames.MatchingGame
import dev.u9g.minigames.mm
import dev.u9g.minigames.runSync
import dev.u9g.minigames.util.EventListener
import dev.u9g.minigames.util.showRules
import dev.u9g.minigames.util.throwablerenderer.sendToOps
import org.bukkit.Bukkit
import org.bukkit.WorldCreator
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.world.WorldInitEvent

class GatheringGame(private val player: Player) {
    companion object {
        fun start(player: Player) {
            // TODO: Make a player -> Game map
            showRules("Gathering", listOf("Gather as much dirt as you can in 2 minutes".mm()), player)
                .thenAccept { it.ifFinished { GatheringGame(player) } }
                .whenComplete { _, err -> err?.sendToOps() }
        }

        fun initListeners() {
            EventListener(WorldInitEvent::class.java, EventPriority.HIGHEST) {
                if (it.world.name.startsWith("matching-")) {
                    it.world.keepSpawnInMemory = false
                    it.world.isAutoSave = false
                    Bukkit.broadcast("not keeping world in memory...".mm())
                }
            }
        }
    }
}
