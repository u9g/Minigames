package dev.u9g.minigames.games.gathering.itemmodifiers.appliableitems.appliables

import com.destroystokyo.paper.MaterialTags
import dev.u9g.minigames.games.gathering.itemmodifiers.appliableitems.PrebuiltAppliable
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment

class SoulsandAppliable : PrebuiltAppliable.EnchantmentAppliable(
        mainItem = MaterialTags.BOOTS,
        materialToAddMore = Material.SOUL_SAND,
        maxLevel = 1,
        enchantment = Enchantment.SOUL_SPEED,
        materialColor = "color:#9f6934"
)