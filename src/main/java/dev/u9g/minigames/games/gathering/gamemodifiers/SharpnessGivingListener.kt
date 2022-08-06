package dev.u9g.minigames.games.gathering.gamemodifiers

import com.destroystokyo.paper.MaterialTags
import com.github.u9g.u9gutils.ItemBuilder
import com.github.u9g.u9gutils.NBTUtil
import dev.u9g.minigames.games.Games
import dev.u9g.minigames.games.gathering.level
import dev.u9g.minigames.util.contains
import org.bukkit.NamespacedKey
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent

private val ENTITIES_HIT = NamespacedKey.fromString("minigames:entities_hit")!!

class SharpnessGivingListener : Listener {
    @EventHandler
    fun onHitEntity(event: EntityDamageByEntityEvent) {
        val player = event.damager
        if (player is Player && Games.isPlayerInGame(player)) {
            val item = player.inventory.itemInMainHand
            if (item.type.isAir || (item !in MaterialTags.SWORDS && item !in MaterialTags.AXES)) return
            val ib = ItemBuilder.from(item)
            val num = NBTUtil.getAsInt(item.itemMeta, ENTITIES_HIT).orElse(0) + 1
            ib.set(ENTITIES_HIT, num)
            val lvl = (num / 10).coerceAtMost(10)
            if (item.level(Enchantment.DAMAGE_ALL) != lvl) {
                ib.enchant(Enchantment.DAMAGE_ALL, lvl)
            }
        }
    }
}