package dev.u9g.minigames.games.getblock

import com.github.shynixn.mccoroutine.bukkit.ticks
import kotlinx.coroutines.delay
import org.bukkit.Material
import org.bukkit.entity.HumanEntity
import org.bukkit.entity.Player

object GetBlockUtil {
    suspend fun hasWantedMaterial(material: Material, player: HumanEntity): Boolean {
        if (player !is Player) return false
        delay(1.ticks)
        return player.itemOnCursor.type == material || player.inventory.contains(material)
    }
}