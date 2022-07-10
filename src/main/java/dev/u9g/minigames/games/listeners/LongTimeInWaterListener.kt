package dev.u9g.minigames.games.listeners

import dev.u9g.minigames.Minigames
import dev.u9g.minigames.util.Task
import net.kyori.adventure.util.Ticks
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import java.net.http.WebSocket.Listener

private val DG = PotionEffectType.DOLPHINS_GRACE
private val WB = PotionEffectType.WATER_BREATHING

class LongTimeInWaterListener : Listener {
    private fun start() {
        // TODO: Only put players in this when they are in gathering game, remove them when they leave
        val secInWater: MutableMap<Player, Int> = mutableMapOf()
        val timeOutOfWater: MutableMap<Player, Int> = mutableMapOf()

        // TODO: Give dolphins grace & water breathing
        Task.syncRepeating(0, Ticks.TICKS_PER_SECOND.toLong()) {
            Bukkit.getOnlinePlayers().stream().filter { it in Minigames.activeGames }.forEach { player ->
                if (player.isInWater) {
                    val secondsInWater = (secInWater[player] ?: 0) + 1
                    secInWater[player] = secondsInWater
                    timeOutOfWater[player] = 0
                    if ((secondsInWater % 10) == 0 && (secondsInWater / 10) <= 3) {
                        player.addPotionEffect(PotionEffect(DG, 10_000_000, secondsInWater / 10))
                        player.addPotionEffect(PotionEffect(WB, 10_000_000, secondsInWater / 10))
                    }
                } else if (player.hasPotionEffect(DG)) {
                    timeOutOfWater[player] = timeOutOfWater.getOrDefault(player, 0) + 1
                    if (timeOutOfWater[player] == 5) {
                        player.removePotionEffect(DG)
                        player.removePotionEffect(WB)
                        timeOutOfWater.remove(player)
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