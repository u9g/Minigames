package dev.u9g.minigames.games.gathering

import com.destroystokyo.paper.MaterialSetTag
import com.destroystokyo.paper.MaterialTags
import com.github.u9g.u9gutils.ItemBuilder
import com.github.u9g.u9gutils.NBTUtil
import dev.u9g.minigames.Minigames
import dev.u9g.minigames.games.gathering.itemmodifiers.remakeLore
import dev.u9g.minigames.games.gathering.util.GATHERING_WORLD_PREFIX
import dev.u9g.minigames.util.*
import net.kyori.adventure.util.Ticks
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.block.BlockFace
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.inventory.*
import org.bukkit.event.player.PlayerAdvancementDoneEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.event.world.WorldInitEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType

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