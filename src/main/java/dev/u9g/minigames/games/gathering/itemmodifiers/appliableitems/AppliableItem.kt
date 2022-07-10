package dev.u9g.minigames.games.gathering.itemmodifiers.appliableitems

import com.destroystokyo.paper.MaterialSetTag
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

interface AppliableItem {
    val mainItem: MaterialSetTag
    val appliedItem: MaterialSetTag
    fun onApply(mainItem: ItemStack, appliedItem: ItemStack, amountOfItemsToUse: AmountOfItemsToUse, player: Player)
}
