package dev.u9g.minigames.games.gathering

import com.github.u9g.u9gutils.ItemBuilder
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemStack

class GatheringWorldListeners {
    companion object {
        fun start() {

        }
    }
}

fun ItemStack.level(enchantment: Enchantment) = this.itemMeta.getEnchantLevel(enchantment)
fun ItemStack.level(enchantment: Enchantment, newLevel: Int) {
    ItemBuilder.from(this).enchant(enchantment, newLevel)
}