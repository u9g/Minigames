package dev.u9g.minigames.games.gathering.itemmodifiers.appliableitems.appliables

import com.destroystokyo.paper.MaterialTags
import dev.u9g.minigames.games.gathering.itemmodifiers.appliableitems.PrebuiltAppliable
import dev.u9g.minigames.util.EventListener
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.inventory.EquipmentSlot

class RedstoneAppliable : PrebuiltAppliable.PDCEnchantmentAppliable(
        mainItem = MaterialTags.HELMETS,
        headerName = "Fiery Crown",
        whatsGiven = "resistance to fire damage",
        quantityGiven = "5%",
        materialToAddMore = Material.REDSTONE,
        maxLevel = 10,
        levelKey = NamespacedKey.fromString("minigames:fiery_crown")!!,
        materialColor = "red") {
    init {
        EventListener(EntityDamageEvent::class.java) {
            when (it.cause) {
                EntityDamageEvent.DamageCause.FIRE,
                EntityDamageEvent.DamageCause.FIRE_TICK,
                EntityDamageEvent.DamageCause.MELTING,
                EntityDamageEvent.DamageCause.LAVA -> {
                    val ent = it.entity
                    if (ent is Player) {
                        val lvl = ent.inventory.getItem(EquipmentSlot.HEAD).let { item -> level(item, this.levelKey) }
                        it.damage *= 1 - (0.05 * lvl)
                    }
                }
                else -> {}
            }
        }
    }
}