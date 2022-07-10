package dev.u9g.minigames.games.gathering.itemmodifiers

import com.destroystokyo.paper.MaterialSetTag
import com.destroystokyo.paper.MaterialTags
import com.github.u9g.u9gutils.ItemBuilder
import dev.u9g.minigames.games.gathering.level
import dev.u9g.minigames.util.contains
import dev.u9g.minigames.util.mm
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemStack

private enum class LoreMaker(val items: MaterialSetTag, val remakeLore: (ItemStack) -> Unit) {
    PICKAXE (MaterialTags.PICKAXES, { item ->
        val maxUnbreakingLevel = 3
        val unbreakingLevel = item.level(Enchantment.DURABILITY)
        val unbreakingLevelsLeft = (maxUnbreakingLevel-unbreakingLevel).coerceAtLeast(0)
        val unbreakingLevelColor = if (unbreakingLevel == 3) "red" else "green"

        val lines = mutableListOf(
                "<white><b>Unbreaking </b><gray>(<white><b><$unbreakingLevelColor>${unbreakingLevelsLeft}</b> Left</white>)".mm(),
                " <gray>Apply <yellow><lang:${Material.SUGAR_CANE.translationKey()}></yellow> to this tool".mm(),
                " → Gives <green>+1</green> unbreaking level".mm(),
                "<white><b>Durability".mm(),
                " <gray>Apply <white>a snowball</white> to this tool".mm(),
                " → Gives <green>+5%</green> durability".mm()
        )
        if (item.enchantments.keys.size > 0)
            lines.add(0, "".mm())
        ItemBuilder.from(item).lore(lines).build()
    });
}

fun remakeLore(item: ItemStack) {
    for (loreMaker in LoreMaker.values()) {
        if (item in loreMaker.items)
            loreMaker.remakeLore(item)
    }
}
