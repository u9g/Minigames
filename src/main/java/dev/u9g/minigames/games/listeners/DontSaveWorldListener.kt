package dev.u9g.minigames.games.listeners

import dev.u9g.minigames.games.gathering.util.GATHERING_WORLD_PREFIX
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.world.WorldInitEvent

class DontSaveWorldListener : Listener {
    @EventHandler
    fun onWorldInit(event: WorldInitEvent) {
        if (!event.world.name.startsWith(GATHERING_WORLD_PREFIX)) return

        event.world.keepSpawnInMemory = false
        event.world.isAutoSave = false
    }
}