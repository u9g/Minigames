package dev.u9g.minigames.games.gathering.util

import dev.u9g.minigames.util.mm
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.generator.ChunkGenerator
import java.util.*

class BetterSpawnChunkGenerator : ChunkGenerator() {
    override fun shouldGenerateNoise() = true

    override fun shouldGenerateSurface() = true

    override fun shouldGenerateBedrock() = true

    override fun shouldGenerateCaves() = true

    override fun shouldGenerateDecorations() = true

    override fun shouldGenerateMobs() = true

    override fun shouldGenerateStructures() = true

    override fun getFixedSpawnLocation(world: World, random: Random): Location {
//        world.getChunkAtAsync(0, 0).thenAccept {
//            var y = world.maxHeight
//            while (it.getBlock(0, y-1, 0).type == Material.AIR) {
//                y--
//            }
//        }
         return Location(world, 0.0, 70.0, 0.0)
//        val a = System.currentTimeMillis()
//        val b = world.getHighestBlockAt(0,0).location.also { it.y++ }
//        Bukkit.broadcast("it took ${(System.currentTimeMillis()-a).toDouble()/1000} sec to find block to spawn at".mm())
//        return b
    }
}