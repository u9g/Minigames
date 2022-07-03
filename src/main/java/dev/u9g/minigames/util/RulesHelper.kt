package dev.u9g.minigames.util

import com.github.u9g.u9gutils.ItemBuilder
import dev.u9g.minigames.*
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import redempt.redlib.inventorygui.InventoryGUI
import java.util.concurrent.CompletableFuture

//private val HEAD_TEXTURES = mapOf(
//    "R" to "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYTVjZWQ5OTMxYWNlMjNhZmMzNTEzNzEzNzliZjA1YzYzNWFkMTg2OTQzYmMxMzY0NzRlNGU1MTU2YzRjMzcifX19",
//    "U" to "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNjA3ZmJjMzM5ZmYyNDFhYzNkNjYxOWJjYjY4MjUzZGZjM2M5ODc4MmJhZjNmMWY0ZWZkYjk1NGY5YzI2In19fQ==",
//    "L" to "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMzE5ZjUwYjQzMmQ4NjhhZTM1OGUxNmY2MmVjMjZmMzU0MzdhZWI5NDkyYmNlMTM1NmM5YWE2YmIxOWEzODYifX19",
//    "E" to "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZGJiMjczN2VjYmY5MTBlZmUzYjI2N2RiN2Q0YjMyN2YzNjBhYmM3MzJjNzdiZDBlNGVmZjFkNTEwY2RlZiJ9fX0=",
//    "S" to "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvM2U0MWM2MDU3MmM1MzNlOTNjYTQyMTIyODkyOWU1NGQ2Yzg1NjUyOTQ1OTI0OWMyNWMzMmJhMzNhMWIxNTE3In19fQ==")

const val EMERALD_HEAD_TEXTURE = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYWM5MDZkNjg4ZTY1ODAyNTY5ZDk3MDViNTc5YmNlNTZlZGM4NmVhNWMzNmJkZDZkNmZjMzU1MTZhNzdkNCJ9fX0="

private fun makeFillerItem(stackSize: Int) = makeItem(material = Material.WHITE_STAINED_GLASS_PANE, count = stackSize)

fun showRules(gameName: String, helpLore: List<Component>, player: Player): CompletableFuture<TaskResult> {
    val cf = CompletableFuture<TaskResult>()

    val head = makeItem(material = Material.PLAYER_HEAD, headTexture = EMERALD_HEAD_TEXTURE, name = "<gradient:aqua:red>Game Info".mm(), lore = helpLore)

    fun updateHelpInventory(inventory: Inventory, bgItem: ItemStack) = inventory.draw(
        listOf("o" * 9,
            "o" + "i".repeat(7) + "o",
            "o" * 9),
        mapOf('o' to bgItem,
            'i' to head)
    )

    val rulesInventory = Bukkit.createInventory(null, 27, gameName.mm())
    val rulesGUI = InventoryGUI(rulesInventory)
    updateHelpInventory(rulesInventory, makeFillerItem(10))
    rulesGUI.open(player)
    val countdown = TickingCountdown(
        endAfterSeconds = 10,
        onTick = { updateHelpInventory(rulesInventory, makeFillerItem(-(it.timesRun-10))) },
        onTimeout = {
            rulesGUI.setOnDestroy {}
            rulesGUI.destroy()
            rulesGUI.inventory.close()
            cf.complete(TaskResult.LEFT_TASK)
        }
    )
    rulesGUI.setOnDestroy {
        countdown.cancel()
        cf.complete(TaskResult.LEFT_TASK)
    }
    return cf
}

enum class TaskResult {
    FINISHED_TASK, LEFT_TASK;

    fun ifFinished(cb: () -> Unit) {
        if (this == FINISHED_TASK) cb()
    }
}
