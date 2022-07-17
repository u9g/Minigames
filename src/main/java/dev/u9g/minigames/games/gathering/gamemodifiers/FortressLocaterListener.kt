package dev.u9g.minigames.games.gathering.gamemodifiers

import com.github.u9g.u9gutils.NBTUtil
import dev.u9g.minigames.makeItem
import dev.u9g.minigames.util.mm
import org.bukkit.*
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerChangedWorldEvent
import org.bukkit.inventory.ShapedRecipe
import org.bukkit.inventory.meta.CompassMeta

val NETHER_FORTRESS_COMPASS = NamespacedKey.fromString("minigames:nether_fortress_compass")!!

class FortressLocaterListener : Listener {
    @EventHandler
    fun onSwitchWorld (event: PlayerChangedWorldEvent) {
        if (event.player.world.environment != World.Environment.NETHER) return
        val nearest = event.player.world.locateNearestStructure(event.player.location, StructureType.NETHER_FORTRESS, Int.MAX_VALUE, false)
                ?: throw Error("Unable to find a fortress")
        val shouldSendMessage = event.player.inventory.filterNotNull().filter { it.itemMeta != null && NBTUtil.getAsBoolean(it.itemMeta, NETHER_FORTRESS_COMPASS).orElse(false) }.map {
            it.editMeta { im ->
                if (im is CompassMeta) {
                    im.lodestone = nearest
                    im.isLodestoneTracked = false
                }
            }
        }.isNotEmpty()

        if (shouldSendMessage)
            event.player.sendMessage("All of your nether compasses have been adjusted!")
    }

    init {
        Bukkit.addRecipe(
                ShapedRecipe(NETHER_FORTRESS_COMPASS,
                        makeItem(material = Material.COMPASS,
                                name = "<red>Nether Compass".mm(),
                                lore = listOf("<gray>Hint: Targets the nether fortress".mm(), "<gray>closest to your nether portal.".mm()),
                                pdc = mapOf(NETHER_FORTRESS_COMPASS to 1.toByte())
                        )).shape(" O ", "OFO", " O ").setIngredient('O', Material.OBSIDIAN).setIngredient('F', Material.FLINT_AND_STEEL),
        )
    }
}