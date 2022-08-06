package dev.u9g.minigames.games.gathering.gamemodifiers

import com.destroystokyo.paper.MaterialSetTag
import dev.u9g.minigames.games.Games
import dev.u9g.minigames.util.contains
import dev.u9g.minigames.util.runSync
import org.bukkit.NamespacedKey
import org.bukkit.block.BlockFace
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent

private val LOGS = MaterialSetTag(NamespacedKey.fromString("minigames:logs")).add(MaterialSetTag.LOGS)
private val DIRECTIONS = setOf(BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST, BlockFace.UP, BlockFace.DOWN).map { it.direction }

class TreefellerListener : Listener {
    @EventHandler
    fun onBlockBreak(event: BlockBreakEvent) {
        if (!Games.isPlayerInGame(event.player)) return

        if (event.block in LOGS)
            for (direction in DIRECTIONS) {
                val newBlock = event.block.location.clone().add(direction).block
                if (newBlock.type == event.block.type) {
                    // This is such a hack, but if we dont run this in a block,
                    // we get into infinite recursion since this spawns another
                    // blockbreakevent, which we then get into, causing a stack
                    // overflow
                    runSync { event.player.breakBlock(newBlock) }
                }
            }
    }
}