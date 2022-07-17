package dev.u9g.minigames.games.gathering.util

import dev.u9g.minigames.util.throwablerenderer.sendToOps
import org.bukkit.attribute.Attribute
import org.bukkit.entity.Player
import org.bukkit.potion.PotionEffect
import java.util.concurrent.CompletableFuture

class PlayerData(private val player: Player) {
    private val location = player.location
    private val exp = ExperienceData(player)
    private val inv = InventoryData(player)
    private val hp = HealthData(player)

    fun resetPlayerData() {
        exp.clearData()
        inv.clearData()
        hp.clearData()
    }

    fun resetPlayerToSnapshot(): CompletableFuture<Unit> {
        val cf = CompletableFuture<Unit>()
        player.sliver_teleportAsync(location).thenAccept {
            if (!it.isSuccessful) {
                val err = Exception("Player failed to teleport due to ${it.name}")
                err.sendToOps()
                err.printStackTrace()
            }
            exp.restoreData()
            inv.restoreData()
            hp.restoreData()
            cf.complete(null)
        }
        return cf
    }
}

private interface Data {
    fun restoreData()
    fun clearData()
}

private class ExperienceData(val player: Player): Data {
    private val lvl = player.level
    private val xp = player.exp

    override fun restoreData() {
        player.level = lvl
        player.exp = xp
    }

    override fun clearData() {
        player.level = 0
        player.exp = 0.0f
    }
}

private class InventoryData(val player: Player): Data {
    private val inventory = player.inventory.contents.map { it?.clone() }
    private val cursorItem = player.itemOnCursor
    private val enderChest = player.enderChest.contents.map { it?.clone() }
    override fun restoreData() {
        for (i in 0 until player.inventory.size) {
            player.inventory.setItem(i, inventory[i])
        }
        player.setItemOnCursor(cursorItem)
        for (i in 0 until player.enderChest.size) {
            player.enderChest.setItem(i, enderChest[i])
        }
    }

    override fun clearData() {
        player.inventory.clear()
        player.setItemOnCursor(null)
        player.enderChest.clear()
    }
}

private class HealthData(val player: Player): Data {
    private val health = player.health
    private val food = player.foodLevel
    private val saturation = player.saturation
    private val potions = player.activePotionEffects.map { it.serialize() }
    private val fireTicks = player.fireTicks
    private val invulnerable = player.isInvulnerable

    override fun restoreData() {
        player.health = health
        player.foodLevel = food
        player.saturation = saturation
        potions.map { PotionEffect(it) }.forEach { player.addPotionEffect(it) }
        player.fireTicks = fireTicks
        player.isInvulnerable = invulnerable
    }

    override fun clearData() {
        player.health = player.getAttribute(Attribute.GENERIC_MAX_HEALTH)!!.value
        player.foodLevel = 20
        player.saturation = 5.0f
        player.activePotionEffects.forEach { player.removePotionEffect(it.type) }
        player.fireTicks = 0
        player.isInvulnerable = false
    }
}