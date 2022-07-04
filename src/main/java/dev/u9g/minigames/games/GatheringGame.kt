package dev.u9g.minigames.games

import dev.u9g.minigames.getCallingPlugin
import dev.u9g.minigames.mm
import dev.u9g.minigames.runSync
import dev.u9g.minigames.util.EventListener
import dev.u9g.minigames.util.infodisplay.showInfoUntilCallbackCalled
import org.bukkit.*
import org.bukkit.block.Biome
import org.bukkit.block.data.BlockData
import org.bukkit.entity.Player
import org.bukkit.event.EventPriority
import org.bukkit.event.world.WorldInitEvent
import org.bukkit.generator.ChunkGenerator
import org.bukkit.generator.WorldInfo
import org.bukkit.material.MaterialData
import org.bukkit.scheduler.BukkitRunnable
import java.util.*

class GatheringGame(private val player: Player): Game {
    private fun startGame() {
        Bukkit.broadcast("started making world".mm())
        val time = System.currentTimeMillis()
        Bukkit.createWorld(WorldCreator.name("matching-${UUID.randomUUID()}"))?.let {
            player.teleportAsync(it.spawnLocation).whenComplete { _, err ->
                err ?: runSync {
                    Bukkit.broadcast(("Done making world after: " + (System.currentTimeMillis()-time).toDouble()/1000 + " seconds").mm())
                }
            }
        } ?: run {
            throw Error("Failed to make world for GatheringGame")
        }
    }

    override fun start() {
        // TODO: Make Player -> Game Map
        val cb = showInfoUntilCallbackCalled(GameInfo.name(), GameInfo.info(), player)
        object : BukkitRunnable() {
            override fun run() {
                cb()
                player.teleportAsync(Bukkit.getWorld("gathering-world")!!.getHighestBlockAt(1000, 1000).location.also { it.y += 1 })
            }
        }.runTaskLater(getCallingPlugin(), 20*3)
    }

    companion object {
        val GameInfo = object : GameInfo {
            override fun name() = "Gathering"

            override fun info() = listOf("Gather as much dirt as you can in 2 minutes".mm())

            override fun start(player: Player): Game {
                // TODO: Make a player -> Game map
                val game = GatheringGame(player)
                game.start()
                return game
            }
        }
    }
}
