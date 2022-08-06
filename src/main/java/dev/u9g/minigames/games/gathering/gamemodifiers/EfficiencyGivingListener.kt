package dev.u9g.minigames.games.gathering.gamemodifiers

import com.destroystokyo.paper.MaterialTags
import com.github.u9g.u9gutils.ItemBuilder
import com.github.u9g.u9gutils.NBTUtil
import dev.u9g.minigames.games.Games
import dev.u9g.minigames.games.gathering.itemmodifiers.remakeLore
import dev.u9g.minigames.games.gathering.level
import dev.u9g.minigames.util.contains
import org.bukkit.NamespacedKey
import org.bukkit.enchantments.Enchantment
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.inventory.ItemStack

private val BLOCKS_BROKEN = NamespacedKey.fromString("minigames:blocks_broken")!!
private const val MAX_LVL = 8

val getEfficiencyLevel: (item: ItemStack) -> XPProgress = {
    val blocksBroken = NBTUtil.getAsInt(it.itemMeta, BLOCKS_BROKEN).orElse(0)
    val lvl = (blocksBroken / 50).coerceAtMost(MAX_LVL)
    var prog = (blocksBroken - (lvl.toDouble() * 50)) / 50
    if (lvl == MAX_LVL) {
        prog = 1.0
    }
    XPProgress(lvl, prog)
}

data class XPProgress(val level: Int, val progress: Double)

class EfficiencyGivingListener : Listener {
    @EventHandler
    fun onBlockBreak(event: BlockBreakEvent) {
        if (!Games.isPlayerInGame(event.player)) return

        val item = event.player.inventory.itemInMainHand
        if (item.type.isAir || (item !in MaterialTags.PICKAXES && item !in MaterialTags.AXES && item !in MaterialTags.SHOVELS)) return
        val ib = ItemBuilder.from(item)
        val blocksBroken = NBTUtil.getAsInt(item.itemMeta, BLOCKS_BROKEN).orElse(0) + 1
        ib.set(BLOCKS_BROKEN, blocksBroken)
        val (lvl, _) = getEfficiencyLevel(item)
        if (item.level(Enchantment.DIG_SPEED) != lvl) {
            ib.enchant(Enchantment.DIG_SPEED, lvl)
        }
        remakeLore(item)
    }
}