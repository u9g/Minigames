package dev.u9g.minigames.games.gathering.itemmodifiers.appliableitems

import com.destroystokyo.paper.MaterialSetTag
import net.kyori.adventure.text.Component
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

interface AppliableItem {
    val mainItem: MaterialSetTag
    val appliedItem: MaterialSetTag
    fun onApply(mainItem: ItemStack, appliedItem: ItemStack, amountOfItemsToUse: AmountOfItemsToUse, player: Player)
    fun loreToAdd(item: ItemStack): List<Component>
    fun enchantLoreToAdd(item: ItemStack): List<Component>
}
