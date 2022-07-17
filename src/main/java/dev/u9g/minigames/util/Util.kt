package dev.u9g.minigames.util

import com.destroystokyo.paper.MaterialSetTag
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.block.Block
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.Plugin
import org.bukkit.plugin.java.JavaPlugin
import java.util.*

fun String.mm() = MiniMessage.miniMessage().deserialize(this)

fun getCallingPlugin(): Plugin {
    val ex = Exception()
    return try {
        val clazz = Class.forName(ex.stackTrace[2].className)
        val plugin: Plugin = JavaPlugin.getProvidingPlugin(clazz)
        if (plugin.isEnabled) plugin else Bukkit.getPluginManager().getPlugin(plugin.name)
    } catch (e: ClassNotFoundException) {
        e.printStackTrace()
        null
    } as Plugin
}

fun Inventory.draw(patterns: List<String>, replacements: Map<Char, ItemStack>) {
    val patternSize = patterns.map { it.length }.fold(0) { a, b -> a + b }
    if (this.size > patternSize) {
        throw IllegalArgumentException("Pattern doesn't match inventory size, pattern size: $patternSize , inventory size: ${this.size}")
    }
    val charsInPatterns = patterns.fold("") { a, b -> a+b}.groupBy { it }.keys.toSet()
    val replacementChars = replacements.keys.toSet()
    if ((charsInPatterns - replacementChars).isNotEmpty()) {
        throw IllegalArgumentException("Replacement map is missing chars from the patterns")
    }

    var i = 0
    for (row in patterns) {
        for (c in row) {
            this.setItem(i++, replacements[c])
        }
    }
}

operator fun String.times(n: Int) = this.repeat(n)

fun runSync (cb: () -> Unit) = Task.syncDelayed { cb() }

operator fun MaterialSetTag.contains(material: Material?): Boolean = material?.let { this.isTagged(it) } ?: false
operator fun MaterialSetTag.contains(item: ItemStack?): Boolean = item?.let { this.isTagged(it) } ?: false
operator fun MaterialSetTag.contains(item: Block?): Boolean = item?.let { this.isTagged(it) } ?: false

private val numbers = linkedMapOf(
        1000 to "M",
        900 to "CM",
        500 to "D",
        400 to "CD",
        100 to "C",
        90 to "XC",
        50 to "L",
        40 to "XL",
        10 to "X",
        9 to "IX",
        5 to "V",
        4 to "IV",
        1 to "I"
)

fun convertToRoman(number: Int): String {
    for (i in numbers.keys){
        if (number >= i) {
            return numbers[i] + convertToRoman(number - i)
        }
    }
    return ""
}

fun Material.toSetTag() = MaterialSetTag(NamespacedKey.fromString("minigames:${this.name.lowercase(Locale.getDefault())}")).add(this)