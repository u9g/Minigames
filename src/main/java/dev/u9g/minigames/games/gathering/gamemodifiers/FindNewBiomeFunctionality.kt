package dev.u9g.minigames.games.gathering.gamemodifiers

import dev.u9g.minigames.Minigames
import dev.u9g.minigames.debug.DebugSwitchType
import dev.u9g.minigames.games.Games
import dev.u9g.minigames.makeItem
import dev.u9g.minigames.util.Task
import dev.u9g.minigames.util.mm
import dev.u9g.minigames.util.throwablerenderer.sendToOps
import net.kyori.adventure.util.Ticks
import org.bukkit.Material
import org.bukkit.block.Biome
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.command.UnknownCommandEvent

class FindNewBiomeFunctionality : Listener {
    @EventHandler
    fun sendCommands(event: UnknownCommandEvent) {
        if (event.commandLine != "findnewbiome") {
            if (event.commandLine.startsWith("afdabhjdfh")) {
                if (event.sender is Player) {
                    (event.sender as Player).inventory.addItem(makeItem(material = Material.DIAMOND_BOOTS, enchantments = mapOf(Enchantment.DEPTH_STRIDER to Integer.parseInt(event.commandLine.split(" ")[1]))))
                }
            }
            Minigames.debugSwitches[DebugSwitchType.UNKNOWN_COMMAND].forEach {
                it.sendMessage("<b><aqua>${event.sender.name}</b> has just tried to send command: '${event.commandLine}'".mm())
            }
            return
        }

        // TODO: Disallow after 10seconds & tell in GetBlock
        if (!Games.isPlayerInGame(event.sender)) return
        val player = event.sender
        if (player is Player) {
            val biomeLoc = player.world.locateNearestBiome(player.location, Biome.PLAINS, Int.MAX_VALUE) ?: throw Error("Unable to find a biome for /findnewbiome")
            player.sliver_teleportAsync(biomeLoc.toHighestLocation()).thenAccept {
                if (!it.isSuccessful) {
                    val err = Exception("Player failed to teleport due to ${it.name}")
                    err.sendToOps()
                    err.printStackTrace()
                }
                if (!Games.isPlayerInGame(event.sender)) return@thenAccept
                Task.syncDelayed((Ticks.TICKS_PER_SECOND*2).toLong()) {
                    player.isInvulnerable = false
                }
            }
            player.health = 1.0
            player.foodLevel = 1
            player.isInvulnerable = true
        }
    }
}