package dev.u9g.minigames.debug

import com.google.common.collect.Multimap
import dev.u9g.minigames.Minigames
import dev.u9g.minigames.util.EventListener
import dev.u9g.minigames.util.mm
import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryDragEvent

class DebugListener {
    companion object {
        fun start() {
            EventListener(InventoryClickEvent::class.java) {
                val one = "<light_purple><b>Click</b></light_purple> -> <aqua><b>Type</aqua>: <green>${it.click.name}</green> | <aqua><b>Action</aqua>: <red>${it.action.name}</red>".mm()
                val two = "cursor: ${it.cursor?.type?.name ?: "AIR"} clicked: ${it.currentItem?.type?.name ?: "AIR"}".mm()
                Minigames.debugSwitches[DebugSwitchType.INVENTORY_CLICK].forEach { player ->
                    player.sendMessage(one)
                    player.sendMessage(two)
                }
            }

            EventListener(InventoryDragEvent::class.java) {
                val one = "${it.whoClicked} dragclicked".mm()
                Minigames.debugSwitches[DebugSwitchType.INVENTORY_CLICK].forEach { player ->
                    player.sendMessage(one)
                }
            }

            EventListener(EntityDamageEvent::class.java) {
                if (Minigames.debugSwitches[DebugSwitchType.DAMAGE_TAKEN].contains(it.entity)) {
                    (it.entity as Player).sendMessage("You have just <b><light_purple>taken damage</b> by <red>${it.cause}".mm())
                }
            }
        }
    }
}