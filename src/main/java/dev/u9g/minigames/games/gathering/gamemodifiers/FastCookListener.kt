package dev.u9g.minigames.games.gathering.gamemodifiers

import dev.u9g.minigames.games.gathering.util.GATHERING_WORLD_PREFIX
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.FurnaceBurnEvent
import org.bukkit.event.inventory.FurnaceStartSmeltEvent

class FastCookListener : Listener {
    @EventHandler
    fun onStartSmelt(event: FurnaceStartSmeltEvent) {
        if (!event.block.world.name.startsWith(GATHERING_WORLD_PREFIX)) return

        event.totalCookTime /= 5
    }

    @EventHandler
    fun onSmeltFuel(event: FurnaceBurnEvent) {
        if (!event.block.world.name.startsWith(GATHERING_WORLD_PREFIX)) return

        event.burnTime /= 5
    }
}