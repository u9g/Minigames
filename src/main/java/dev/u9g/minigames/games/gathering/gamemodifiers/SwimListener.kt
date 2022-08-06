package dev.u9g.minigames.games.gathering.gamemodifiers

import dev.u9g.minigames.Minigames
import dev.u9g.minigames.debug.DebugSwitchType
import dev.u9g.minigames.games.Games
import dev.u9g.minigames.util.Task
import dev.u9g.minigames.util.mm
import net.kyori.adventure.util.Ticks
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.Listener
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType

private val DG = PotionEffectType.DOLPHINS_GRACE
private val WB = PotionEffectType.WATER_BREATHING

class SwimListener : Listener {
    private fun start() {
        // TODO: Only put players in this when they are in gathering game, remove them when they leave
        val secInWater: MutableMap<Player, Int> = mutableMapOf()
        val secOutOfWater: MutableMap<Player, Int> = mutableMapOf()

        // TODO: Give dolphins grace & water breathing
        Task.syncRepeating(0, Ticks.TICKS_PER_SECOND.toLong()) {
            Bukkit.getOnlinePlayers().stream().filter { Games.isPlayerInGame(it) }.forEach { player ->
                if (Minigames.debugSwitches[DebugSwitchType.SWIM_SPEED].contains(player)) {
                    val playerInWaterColor = if (!player.isInWater) "red" else "green"
                    val playerInDolphinsGrace = if (!player.isInWater) "red" else "green"
                    Bukkit.broadcast("<$playerInWaterColor>In water?</$playerInWaterColor> | <$playerInDolphinsGrace>Has Dolphins Grace?</$playerInDolphinsGrace> | secInWater: ${secInWater.getOrDefault(player, 0)} | sec out of water: ${secOutOfWater.getOrDefault(player, 0)}".mm())
                }
                if (player.isInWater) {
                    val secondsInWater = (secInWater[player] ?: 0) + 1
                    secInWater[player] = secondsInWater
                    secOutOfWater[player] = 0
                    if (/*(secondsInWater % 10) == 0 && */(secondsInWater / 10) > 0) {
                        // if you spawn in water it glitches
                        player.addPotionEffect(PotionEffect(DG, 10_000_000, (secondsInWater / 10).coerceAtMost(0)))
                        player.addPotionEffect(PotionEffect(WB, 10_000_000, (secondsInWater / 10).coerceAtMost(0)))
                    }
                } else if (player.hasPotionEffect(DG)) {
                    secOutOfWater[player] = secOutOfWater.getOrDefault(player, 0) + 1
                    if (secOutOfWater[player] == 5) {
                        player.removePotionEffect(DG)
                        player.removePotionEffect(WB)
                        secOutOfWater.remove(player)
                        secInWater.remove(player)
                    }
                }
            }
        }
    }

    init {
        start()
    }
}