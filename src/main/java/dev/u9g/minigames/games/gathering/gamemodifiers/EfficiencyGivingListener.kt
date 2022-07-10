package dev.u9g.minigames.games.gathering.gamemodifiers

import com.destroystokyo.paper.MaterialTags
import com.github.u9g.u9gutils.ItemBuilder
import com.github.u9g.u9gutils.NBTUtil
import dev.u9g.minigames.Minigames
import dev.u9g.minigames.games.gathering.itemmodifiers.remakeLore
import dev.u9g.minigames.games.gathering.level
import dev.u9g.minigames.util.contains
import org.bukkit.NamespacedKey
import org.bukkit.enchantments.Enchantment
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent

private val BLOCKS_BROKEN = NamespacedKey.fromString("minigames:blocks_broken")!!

class EfficiencyGivingListener : Listener {
    @EventHandler
    fun onBlockBreak(event: BlockBreakEvent) {
        if (event.player !in Minigames.activeGames) return

        val item = event.player.inventory.itemInMainHand
        if (item.type.isAir || (item !in MaterialTags.PICKAXES && item !in MaterialTags.AXES)) return
        val ib = ItemBuilder.from(item)
        val blocksBroken = NBTUtil.getAsInt(item.itemMeta, BLOCKS_BROKEN).orElse(0) + 1
        ib.set(BLOCKS_BROKEN, blocksBroken)
        val lvl = (blocksBroken / 50).coerceAtMost(8)
        if (item.level(Enchantment.DIG_SPEED) != lvl) {
            ib.enchant(Enchantment.DIG_SPEED, lvl)
            remakeLore(item)
        }
    }
}