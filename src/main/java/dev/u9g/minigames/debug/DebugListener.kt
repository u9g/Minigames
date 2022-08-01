package dev.u9g.minigames.debug

import com.google.common.collect.Multimap
import dev.u9g.minigames.Minigames
import dev.u9g.minigames.util.EventListener
import dev.u9g.minigames.util.mm
import org.apache.commons.lang.StringUtils
import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryDragEvent
import java.util.StringJoiner

class DebugListener {
    companion object {
        fun start() {
            EventListener(InventoryClickEvent::class.java) {
                val data: String = StringUtils.join(mapOf(
                        "Type" to it.click.name,
                        "Action" to it.action.name,
                        "Slot Type" to it.slotType.name
                ).entries.map { "<b><aqua>${it.key}</b>: <green>${it.value}</green>" }, " | ")
                val one = "<light_purple><b>Click</b></light_purple> -> $data".mm()
                val two = "cursor: ${it.cursor?.type?.name ?: "AIR"} clicked: ${it.currentItem?.type?.name ?: "AIR"}".mm()
                Minigames.debugSwitches[DebugSwitchType.INVENTORY_CLICK].forEach { player ->
                    player.sendMessage(one)
                    player.sendMessage(two)
                }
            }

            EventListener(InventoryDragEvent::class.java) {
                val one = "<light_purple><b><red>Drag</red>Click</b></light_purple> -> <aqua><b>Type</aqua>: <green>${it.type}</green>".mm()
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