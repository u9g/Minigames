package dev.u9g.minigames.games.gathering.itemmodifiers.appliableitems.appliables

import com.destroystokyo.paper.MaterialTags
import dev.u9g.minigames.games.gathering.itemmodifiers.appliableitems.PrebuiltAppliable
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment

class CopperAppliable : PrebuiltAppliable.EnchantmentAppliable(
        mainItem = MaterialTags.PICKAXES,
        materialToAddMore = Material.COPPER_INGOT,
        maxLevel = 3,
        enchantment = Enchantment.LOOT_BONUS_BLOCKS,
        materialColor = "gold")