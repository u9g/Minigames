package dev.u9g.minigames.games.gathering.gamemodifiers

import com.destroystokyo.paper.MaterialTags
import dev.u9g.minigames.games.Games
import dev.u9g.minigames.util.contains
import org.bukkit.Location
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent

class ManyBlockBreakListener : Listener {
    private val brokenByBreakBlock = mutableSetOf<Location>()

    @EventHandler
    fun onBlockBreak(event: BlockBreakEvent) {
        if (!Games.isPlayerInGame(event.player)) return

        val item = event.player.inventory.itemInMainHand
        val loc = event.block.location

        if (loc in brokenByBreakBlock) {
            brokenByBreakBlock.remove(loc)
            return
        } else if (item.type in MaterialTags.PICKAXES) {
            val locOne = loc.clone().subtract(0.0, 1.0, 0.0)
            if (locOne.block.type.hardness > 0f && locOne.block.type.hardness != 100f) {
                brokenByBreakBlock.add(locOne)
                event.player.breakBlock(locOne.block)
            }
            val locTwo = loc.clone().subtract(0.0, 2.0, 0.0)
            if (locTwo.block.type.hardness > 0f && locOne.block.type.hardness != 100f) {
                brokenByBreakBlock.add(locTwo)
                event.player.breakBlock(locTwo.block)
            }
        }
    }
}