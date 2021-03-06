package dev.u9g.minigames.games.gathering.itemmodifiers.appliableitems.appliables

import com.destroystokyo.paper.MaterialTags
import dev.u9g.minigames.games.gathering.itemmodifiers.appliableitems.PrebuiltAppliable
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment

class SugarcaneAppliable : PrebuiltAppliable.EnchantmentAppliable(
        mainItem = MaterialTags.PICKAXES,
        materialToAddMore = Material.SUGAR_CANE,
        maxLevel = 3,
        enchantment = Enchantment.DURABILITY,
        materialColor = "yellow"
)