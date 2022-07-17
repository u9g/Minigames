package dev.u9g.minigames.games.gathering.itemmodifiers.appliableitems.appliables

import com.destroystokyo.paper.MaterialTags
import dev.u9g.minigames.games.gathering.itemmodifiers.appliableitems.PrebuiltAppliable
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment

class FlintAppliable : PrebuiltAppliable.EnchantmentAppliable(
        mainItem = MaterialTags.SWORDS,
        materialToAddMore = Material.FLINT,
        maxLevel = 2,
        enchantment = Enchantment.FIRE_ASPECT,
        materialColor = "white"
)