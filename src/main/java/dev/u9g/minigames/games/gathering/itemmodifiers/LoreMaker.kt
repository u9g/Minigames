package dev.u9g.minigames.games.gathering.itemmodifiers

import com.github.u9g.u9gutils.ItemBuilder
import dev.u9g.minigames.Minigames
import dev.u9g.minigames.util.mm
import net.kyori.adventure.text.Component
import org.bukkit.inventory.ItemStack

fun remakeLore(item: ItemStack) {
    val lines = mutableListOf<Component>()

    val enchLines = (Minigames.appliableItemManager?.enchantmentLoreFor(item) ?: emptyList())
    lines.addAll(enchLines)

    if (enchLines.isNotEmpty() || item.enchantments.keys.size > 0) {
        lines.add("".mm())
    }
    lines.addAll(Minigames.appliableItemManager?.loreFor(item)?.toTypedArray() ?: emptyList<Component>().toTypedArray())

    if (lines.isNotEmpty())
        ItemBuilder.from(item).lore(lines)
}
