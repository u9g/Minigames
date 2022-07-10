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
import org.bukkit.inventory.meta.Damageable

val snowballTag = MaterialSetTag(NamespacedKey.fromString("minigames:snowball")).add(Material.SNOWBALL)

class SnowballAppliable(override val mainItem: MaterialSetTag = MaterialTags.PICKAXES,
                         override val appliedItem: MaterialSetTag = snowballTag)
    : AppliableItem {
    override fun onApply(mainItem: ItemStack, appliedItem: ItemStack, amountOfItemsToUse: AmountOfItemsToUse, player: Player) {
        val snowball = appliedItem

        val meta = mainItem.itemMeta
        if (meta is Damageable) {
            val durabilityGivenPerSnowball = (mainItem.type.maxDurability * .05).toInt()
            var maxUsable = (meta.damage / durabilityGivenPerSnowball).coerceAtMost(snowball.amount)
            if (meta.damage % durabilityGivenPerSnowball > 0)
                maxUsable++
            if (maxUsable > 1 && amountOfItemsToUse == AmountOfItemsToUse.ONE)
                maxUsable = 1

            meta.damage -= (maxUsable*durabilityGivenPerSnowball).coerceAtMost(meta.damage)
            mainItem.itemMeta = meta
            snowball.amount -= maxUsable
        }
    }
}