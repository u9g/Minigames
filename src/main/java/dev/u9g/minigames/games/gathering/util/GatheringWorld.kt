package dev.u9g.minigames.games.gathering.util

import dev.u9g.minigames.util.Task
import dev.u9g.minigames.util.mm
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.WorldCreator
import java.util.UUID

const val GATHERING_WORLD_PREFIX = "gathering-"

class GatheringWorld {
    private val worldName = GATHERING_WORLD_PREFIX + UUID.randomUUID()
    private var world: World? = null

    fun makeWorld() {
        world = Bukkit.createWorld(WorldCreator.name(worldName).generator(BetterSpawnChunkGenerator()))
                ?: throw Error("Unable to make world")
    }

    fun spawnPoint(): Location {
        return (world ?: throw Error("called GatheringWorld#spawnPoint with a null world"))
                .getHighestBlockAt(0, 0).location
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