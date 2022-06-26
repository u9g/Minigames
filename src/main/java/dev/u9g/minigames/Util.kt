package dev.u9g.minigames

import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.util.Ticks
import org.bukkit.Bukkit
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.Plugin
import org.bukkit.plugin.java.JavaPlugin
import java.util.concurrent.TimeUnit
import java.util.function.Consumer

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

//fun runAfterSeconds(seconds: Double, cb: Consumer<Task?>) = Task.syncDelayed((Ticks.TICKS_PER_SECOND * seconds).toLong(), cb)
fun runSync (cb: () -> Unit) = Task.syncDelayed { cb() }