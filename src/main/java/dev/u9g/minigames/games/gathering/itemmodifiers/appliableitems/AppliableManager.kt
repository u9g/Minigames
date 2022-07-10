package dev.u9g.minigames.games.gathering.itemmodifiers

import dev.u9g.minigames.Minigames
import dev.u9g.minigames.games.gathering.itemmodifiers.appliableitems.AmountOfItemsToUse
import dev.u9g.minigames.games.gathering.itemmodifiers.appliableitems.AppliableItem
import dev.u9g.minigames.util.contains
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryAction
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.ItemStack

class AppliableItemManager(private val appliables: List<AppliableItem>) : Listener {

    @EventHandler
    fun onClick(event: InventoryClickEvent) {
        if (event.whoClicked !in Minigames.activeGames ) return

        if (event.whoClicked !is Player) {
            throw Error("InventoryClickEvent called with nonplayer")
        }
        val cursor = event.cursor
        val clicked = event.currentItem
        if ((cursor == null || clicked == null) ||
                event.action != InventoryAction.SWAP_WITH_CURSOR) return
        val foundMatchingAppliable = runApplicableAppliableItems(
                cursor,
                clicked,
                if (event.click == ClickType.RIGHT)
                    AmountOfItemsToUse.ONE
                else
                    AmountOfItemsToUse.ALL,
                event.whoClicked as Player)
        if (foundMatchingAppliable) {
            event.isCancelled = true
        }
    }

    /**
     * Returns two if event should be cancelled
     */
    private fun runApplicableAppliableItems(itemOne: ItemStack, itemTwo: ItemStack, amountOfItemsToUse: AmountOfItemsToUse, player: Player): Boolean {
        for (appliable in appliables) {
            if (itemOne in appliable.mainItem && itemTwo in appliable.appliedItem) {
                appliable.onApply(mainItem = itemOne, appliedItem = itemTwo, amountOfItemsToUse = amountOfItemsToUse, player = player)
                remakeLore(itemOne)
                return true
            } else if (itemOne in appliable.appliedItem && itemTwo in appliable.mainItem) {
                appliable.onApply(mainItem = itemTwo, appliedItem = itemOne, amountOfItemsToUse = amountOfItemsToUse, player = player)
                remakeLore(itemTwo)
                return true
            }
        }
        return false
    }
}