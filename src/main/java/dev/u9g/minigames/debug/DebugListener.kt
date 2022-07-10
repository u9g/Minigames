package dev.u9g.minigames.debug

import com.google.common.collect.Multimap
import dev.u9g.minigames.util.EventListener
import dev.u9g.minigames.util.mm
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryDragEvent

class DebugListener {
    companion object {
        fun start(debugSwitches: Multimap<DebugSwitchType, Player>) {
            EventListener(InventoryClickEvent::class.java) {
                val one = "<aqua><b>Click Type</aqua>: <green>${it.click.name}</green> | <aqua><b>Click Action</aqua>: <red>${it.action.name}".mm()
                val two = "cursor: ${it.cursor?.type?.name ?: "AIR"} clicked: ${it.currentItem?.type?.name ?: "AIR"}".mm()
                debugSwitches[DebugSwitchType.INVENTORY_CLICK].forEach { player ->
                    player.sendMessage(one)
                    player.sendMessage(two)
                }
            }

            EventListener(InventoryDragEvent::class.java) {
                val one = "${it.whoClicked} dragclicked".mm()
                debugSwitches[DebugSwitchType.INVENTORY_CLICK].forEach { player ->
                    player.sendMessage(one)
                }
            }
        }
    }
}