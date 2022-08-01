package dev.u9g.minigames.games.gathering.gamemodifiers

import dev.u9g.minigames.Minigames
import dev.u9g.minigames.debug.DebugSwitchType
import dev.u9g.minigames.util.mm
import org.bukkit.GameMode
import org.bukkit.Sound
import org.bukkit.SoundCategory
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.player.PlayerFishEvent
import org.bukkit.inventory.meta.Damageable
import java.util.*
import kotlin.time.Duration.Companion.seconds


object config {
    object cooldown {
        val enabled = false
        val amount = (5).seconds
    }

    object cancelDamage {
        val enabled = true
        val timeoutAfter = (5).seconds
    }

    object message {
        val cooldown = "<red>Whow! Slow down there!".mm()
    }

    object errorSound {
        val enabled = true
    }

    object damageRod {
        val enabled = true
    }
}

// TODO: Clear this at some point...
private var flyingTimeout = HashMap<UUID, Long>()

private val cooldown = HashMap<UUID, Long>()

class GrapplingHookListener : Listener {
    @EventHandler
    fun onFish(event: PlayerFishEvent) {
        val p = event.player
        if (event.state === PlayerFishEvent.State.REEL_IN || event.state === PlayerFishEvent.State.IN_GROUND) {
            if (config.cooldown.enabled) {
                cooldown[p.uniqueId]?.let { cd ->
                    if (cd > System.currentTimeMillis()) {
                        p.sendMessage(config.message.cooldown)
                        if (config.errorSound.enabled) p.playSound(p.location, Sound.ENTITY_ENDERMAN_TELEPORT, SoundCategory.BLOCKS, 2f, 0f)
                        return
                    }
                }
                cooldown[p.uniqueId] = System.currentTimeMillis() + config.cooldown.amount.inWholeMilliseconds

                p.velocity = event.hook.location.subtract(p.location).also { it.y = 1.0 }.toVector()
                if (config.damageRod.enabled && !Minigames.debugSwitches[DebugSwitchType.NO_GRAPPLING_DURA_DMG].contains(p)) p.inventory.itemInMainHand.editMeta {
                    if (it is Damageable) {
                        it.damage = (it.damage + (p.inventory.itemInMainHand.type.maxDurability * .1).toInt()).coerceAtMost(p.inventory.itemInMainHand.type.maxDurability.toInt())
                    }
                }
            } else if (!config.cooldown.enabled) {
                p.velocity = event.hook.location.subtract(p.location).also { it.y = 1.0 }.toVector()
                if (config.damageRod.enabled && !Minigames.debugSwitches[DebugSwitchType.NO_GRAPPLING_DURA_DMG].contains(p)) p.inventory.itemInMainHand.editMeta {
                    if (it is Damageable) {
                        it.damage = (it.damage + (p.inventory.itemInMainHand.type.maxDurability * .1).toInt()).coerceAtMost(p.inventory.itemInMainHand.type.maxDurability.toInt())
                    }
                }
            }
            if (config.cancelDamage.enabled) flyingTimeout[p.uniqueId] = System.currentTimeMillis() + config.cancelDamage.timeoutAfter.inWholeMilliseconds
        }
    }

    @EventHandler
    fun onDamage(event: EntityDamageEvent) {
        val ent = event.entity
        if (ent is Player && ent.gameMode != GameMode.CREATIVE && event.cause == EntityDamageEvent.DamageCause.FALL) {
            flyingTimeout[ent.uniqueId]?.let { cd ->
                if (cd < System.currentTimeMillis()) return
                event.isCancelled = true
            }
        }
    }
}