package dev.u9g.minigames.games.listeners

import dev.u9g.minigames.games.Games
import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryAction
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryDragEvent
import org.bukkit.event.inventory.InventoryType

private val modifyOtherSlotsActions = listOf(
        /*InventoryAction.PICKUP_ALL,*/ InventoryAction.MOVE_TO_OTHER_INVENTORY,
        /*InventoryAction.HOTBAR_MOVE_AND_READD, InventoryAction.HOTBAR_SWAP,*/
        InventoryAction.COLLECT_TO_CURSOR, InventoryAction.UNKNOWN)
// Disallows players to equip heads
class PlayerEquipHeadListener : Listener {
    @EventHandler
    fun click(event: InventoryClickEvent) {
        if (!Games.isPlayerInGame(event.whoClicked)) return

        // TODO: user cant shift click this item now into a chest but I dont care :shrug:
        if ((event.slotType == InventoryType.SlotType.ARMOR && (event.cursor?.type == Material.PLAYER_HEAD || (event.hotbarButton != -1 && event.clickedInventory?.getItem(event.hotbarButton)?.type == Material.PLAYER_HEAD))) ||
            ((event.slotType == InventoryType.SlotType.CONTAINER || event.slotType == InventoryType.SlotType.QUICKBAR) && event.action in modifyOtherSlotsActions && event.currentItem?.type == Material.PLAYER_HEAD)) {
            event.isCancelled = true
        }
    }

    @EventHandler
    fun drag(event: InventoryDragEvent) {
        if (!Games.isPlayerInGame(event.whoClicked)) return

        if (event.rawSlots.any { event.view.getSlotType(it) == InventoryType.SlotType.ARMOR } && event.cursor?.type == Material.PLAYER_HEAD) {
            event.isCancelled = true
        }
    }
}
