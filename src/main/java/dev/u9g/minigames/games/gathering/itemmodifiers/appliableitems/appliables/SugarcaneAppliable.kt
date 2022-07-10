package dev.u9g.minigames.games.gathering.itemmodifiers.appliableitems.appliables

import com.destroystokyo.paper.MaterialSetTag
import com.destroystokyo.paper.MaterialTags
import dev.u9g.minigames.games.gathering.itemmodifiers.appliableitems.AmountOfItemsToUse
import dev.u9g.minigames.games.gathering.itemmodifiers.appliableitems.AppliableItem
import dev.u9g.minigames.games.gathering.level
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

val sugarcaneTag = MaterialSetTag(NamespacedKey.fromString("minigames:sugar_cane")).add(Material.SUGAR_CANE)

class SugarcaneAppliable(override val mainItem: MaterialSetTag = MaterialTags.PICKAXES,
                         override val appliedItem: MaterialSetTag = sugarcaneTag)
    : AppliableItem {
    override fun onApply(mainItem: ItemStack, appliedItem: ItemStack, amountOfItemsToUse: AmountOfItemsToUse, player: Player) {
        val pickaxe = mainItem
        val sugarcane = appliedItem
        val currLvl = pickaxe.level(Enchantment.DURABILITY)
        if (currLvl < 3) {
            var addable = sugarcane.amount.coerceAtMost(3-currLvl)
            if (addable > 1 && amountOfItemsToUse == AmountOfItemsToUse.ONE) {
                addable = 1
            }
            pickaxe.level(Enchantment.DURABILITY, currLvl + addable)
            sugarcane.amount -= addable
        }
    }
}