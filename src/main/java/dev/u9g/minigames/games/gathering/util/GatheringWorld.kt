package dev.u9g.minigames.games.gathering.util

import dev.u9g.minigames.util.Task
import dev.u9g.minigames.util.mm
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import org.bukkit.World
import org.bukkit.WorldCreator
import org.bukkit.entity.Player
import java.util.UUID
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
            player.teleportAsync(loc).thenAccept {
                cf.complete(null)
            }
        })
        return cf
    }

    fun delete() {
        val theWorld = world ?: throw Error("called GatheringWorld#delete with a null world")
        Bukkit.unloadWorld(theWorld, false)
        println("starting delete")
        Task.asyncDelayed(0) {
            if (!theWorld.worldFolder.deleteRecursively())
                Bukkit.broadcast("failed to delete".mm())
            println("done deleting")
        }
    }
}