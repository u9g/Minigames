package dev.u9g.minigames.games.gathering.gamemodifiers

import com.destroystokyo.paper.MaterialSetTag
import com.github.u9g.u9gutils.NBTUtil
import dev.u9g.minigames.games.Games
import dev.u9g.minigames.makeItem
import dev.u9g.minigames.util.mm
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.RecipeChoice
import org.bukkit.inventory.ShapedRecipe

val SINGLE_HEAD = NamespacedKey.fromString("minigames:single_use_crafting_table")!!

class SingleUseCraftingTableListener : Listener {
    @EventHandler
    fun onRightClickCraftingHead(event: PlayerInteractEvent) {
        if (!Games.isPlayerInGame(event.player)) return
        if (event.player.inventory.itemInMainHand.itemMeta?.let { NBTUtil.getAsBoolean(it, SINGLE_HEAD).orElse(false) } != true) {
            return
        }
        if (event.action.isRightClick) {
            event.player.openWorkbench(null, true)
            event.player.inventory.itemInMainHand.amount--
        }
        event.isCancelled = true
    }

    init {
        Bukkit.addRecipe(
                ShapedRecipe(SINGLE_HEAD, makeItem(
                        material = Material.PLAYER_HEAD,
                        name = "<green>Single use crafting table".mm(),
                        lore = listOf("<gray>Hint: Right click this item".mm(), "<gray>for a single use crafting table".mm()),
                        pdc = mapOf(SINGLE_HEAD to 1.toByte()),
                        headTexture = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvY2U3ZDhjMjQyZDJlNGY4MDI4ZjkzMGJlNzZmMzUwMTRiMjFiNTI1NTIwOGIxYzA0MTgxYjI1NzQxMzFiNzVhIn19fQ=="
                )).shape("LLL", "LLL", "LLL").setIngredient('L', RecipeChoice.MaterialChoice(MaterialSetTag.PLANKS))
        )
    }
}
