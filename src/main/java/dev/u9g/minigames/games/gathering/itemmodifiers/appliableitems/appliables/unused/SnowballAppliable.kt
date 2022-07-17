package dev.u9g.minigames.games.gathering.itemmodifiers.appliableitems.appliables.unused

import com.destroystokyo.paper.MaterialSetTag
import com.destroystokyo.paper.MaterialTags
import dev.u9g.minigames.games.gathering.itemmodifiers.appliableitems.AmountOfItemsToUse
import dev.u9g.minigames.games.gathering.itemmodifiers.appliableitems.AppliableItem
import dev.u9g.minigames.games.gathering.itemmodifiers.appliableitems.PrebuiltAppliable
import dev.u9g.minigames.util.toSetTag
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.Damageable

private val snowballTag = Material.SNOWBALL.toSetTag()
private val loreMaker = PrebuiltAppliable.OnLore.Unlimited.custom("Durability", "durability", "5%", Material.SNOWBALL, "white")

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

    override fun loreToAdd(item: ItemStack): List<Component> = loreMaker(item)

    override fun enchantLoreToAdd(item: ItemStack): List<Component> = emptyList()
}