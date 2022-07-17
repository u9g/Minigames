package dev.u9g.minigames.games.gathering.gamemodifiers

import dev.u9g.minigames.games.gathering.util.GATHERING_WORLD_PREFIX
import org.bukkit.entity.EntityType
import org.bukkit.entity.Mob
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.loot.LootContext
import java.util.concurrent.ThreadLocalRandom

private val DONT_LOOTING_ENTITIES: List<EntityType> = listOf()

class LootingDisablerListener : Listener {
    @EventHandler
    fun onEntityDeath(event: EntityDeathEvent) {
        val mob = event.entity
        if (mob.world.name.startsWith(GATHERING_WORLD_PREFIX) &&
                mob.type in DONT_LOOTING_ENTITIES &&
                mob is Mob) {
            val killer = event.entity.killer
            if (killer is Player) {
                val ctx = LootContext.Builder(event.entity.location).lootedEntity(event.entity).lootingModifier(0).killer(killer)
                event.drops.clear()
                mob.lootTable
                        ?.populateLoot(ThreadLocalRandom.current(), ctx.build())
                        ?.let {
                    event.drops.addAll(it)
                }
            }
        }
    }
}