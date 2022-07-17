package dev.u9g.minigames.games.gathering.gamemodifiers

import dev.u9g.minigames.games.gathering.util.GATHERING_WORLD_PREFIX
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockDropItemEvent

private val DONT_FORTUNE_MATERIALS = listOf(Material.FLINT)

class FortuneDisablerListener : Listener {
    @EventHandler
    fun onBlockBreak(event: BlockDropItemEvent) {
        if (!event.block.world.name.startsWith(GATHERING_WORLD_PREFIX)) return

        if (event.block.type !in DONT_FORTUNE_MATERIALS) return

        event.items.clear()

        val noFortuneItem = event.player.inventory.itemInMainHand.clone()
        noFortuneItem.editMeta { it.removeEnchant(Enchantment.LOOT_BONUS_BLOCKS) }
        event.block.getDrops(noFortuneItem, event.player).forEach {
            event.player.world.dropItemNaturally(event.block.location, it)
        }
    }
}
