package dev.u9g.minigames.games.listeners

import com.destroystokyo.paper.MaterialTags
import dev.u9g.minigames.Minigames
import dev.u9g.minigames.games.gathering.itemmodifiers.remakeLore
import dev.u9g.minigames.util.contains
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.PrepareItemCraftEvent

class MakePickaxeLoreListener : Listener {
    @EventHandler
    fun onPrepareItemToCraft(it: PrepareItemCraftEvent) {
        if (it.viewers.first() !in Minigames.activeGames) return

        if (it.inventory.result?.type in MaterialTags.PICKAXES) {
            remakeLore(it.inventory.result!!)
        }
    }
}