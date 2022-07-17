package dev.u9g.minigames.games.gathering.util

import dev.u9g.minigames.util.Task
import dev.u9g.minigames.util.mm
import dev.u9g.minigames.util.throwablerenderer.sendToOps
import net.kyori.adventure.util.Ticks
import org.bukkit.Bukkit
import org.bukkit.World
import org.bukkit.WorldCreator
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerTeleportEvent
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.function.Consumer

const val GATHERING_WORLD_PREFIX = "gathering-"

class GatheringWorld {
    private val worldName = GATHERING_WORLD_PREFIX + UUID.randomUUID()
    private var world: World? = null

    fun makeWorld() {
        world = Bukkit.createWorld(WorldCreator.name(worldName).generator(BetterSpawnChunkGenerator()))
                ?: throw Error("Unable to make world")
    }

    fun teleportToSpawnPoint(player: Player): CompletableFuture<Unit> {
        val cf = CompletableFuture<Unit>()
        val nonNullWorld = (world ?: throw Error("called GatheringWorld#spawnPoint with a null world"))
        val loc = nonNullWorld.getHighestBlockAt(0, 0).location
        nonNullWorld.getChunkAtAsync(loc, Consumer {
            player.sliver_teleportAsync(loc, PlayerTeleportEvent.TeleportCause.PLUGIN).thenAccept {
                if (!it.isSuccessful) {
                    val err = Exception("Player failed to teleport due to ${it.name}")
                    err.sendToOps()
                    err.printStackTrace()
                }
                cf.complete(null)
            }
        })
        return cf
    }

    fun delete() {
        val theWorld = world ?: throw Error("called GatheringWorld#delete with a null world")
        val unloadResult = Bukkit.sliver_unloadWorld(theWorld, false)
        if (!unloadResult.isSuccessful) {
            Bukkit.broadcast("failed to delete due to: $unloadResult".mm(), "isop.isop")
        } else {
            println("starting delete")
            Task.asyncDelayed((Ticks.TICKS_PER_SECOND * 10).toLong()) {
                if (!theWorld.worldFolder.deleteRecursively())
                    Bukkit.broadcast("failed to delete world folder".mm(), "isop.isop")
                println("done deleting")
            }
        }
    }
}