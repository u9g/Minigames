package dev.u9g.minigames.games.gathering.util

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
        return world.getHighestBlockAt(0,0).location.also { it.y++ }
    }
}