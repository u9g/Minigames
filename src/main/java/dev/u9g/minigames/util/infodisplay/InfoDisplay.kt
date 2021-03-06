package dev.u9g.minigames.util.infodisplay

import dev.u9g.minigames.makeItem
import dev.u9g.minigames.util.TickingCountdown
import dev.u9g.minigames.util.draw
import dev.u9g.minigames.util.mm
import dev.u9g.minigames.util.times
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import redempt.redlib.inventorygui.InventoryGUI
import java.util.concurrent.CompletableFuture

//private val HEAD_TEXTURES = mapOf(
//    "R" to "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYTVjZWQ5OTMxYWNlMjNhZmMzNTEzNzEzNzliZjA1YzYzNWFkMTg2OTQzYmMxMzY0NzRlNGU1MTU2YzRjMzcifX19",
//    "U" to "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNjA3ZmJjMzM5ZmYyNDFhYzNkNjYxOWJjYjY4MjUzZGZjM2M5ODc4MmJhZjNmMWY0ZWZkYjk1NGY5YzI2In19fQ==",
//    "L" to "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMzE5ZjUwYjQzMmQ4NjhhZTM1OGUxNmY2MmVjMjZmMzU0MzdhZWI5NDkyYmNlMTM1NmM5YWE2YmIxOWEzODYifX19",
//    "E" to "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZGJiMjczN2VjYmY5MTBlZmUzYjI2N2RiN2Q0YjMyN2YzNjBhYmM3MzJjNzdiZDBlNGVmZjFkNTEwY2RlZiJ9fX0=",
//    "S" to "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvM2U0MWM2MDU3MmM1MzNlOTNjYTQyMTIyODkyOWU1NGQ2Yzg1NjUyOTQ1OTI0OWMyNWMzMmJhMzNhMWIxNTE3In19fQ==")

const val EMERALD_HEAD_TEXTURE = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYWM5MDZkNjg4ZTY1ODAyNTY5ZDk3MDViNTc5YmNlNTZlZGM4NmVhNWMzNmJkZDZkNmZjMzU1MTZhNzdkNCJ9fX0="

val fillerItem = makeItem(material = Material.WHITE_STAINED_GLASS_PANE, name = " ".mm())

class InfoDisplay(gameName: Component,
                  helpLore: List<Component>,
                  player: Player,
                  defaultBackgroundStackSize: Int,
                  private val onClosedByPlayer: () -> Unit){
    private val head = makeItem(material = Material.PLAYER_HEAD, headTexture = EMERALD_HEAD_TEXTURE, name = "<gradient:green:blue>Game Info".mm(), lore = helpLore)
    private val inventory = Bukkit.createInventory(null, 27, gameName)
    private val gui = InventoryGUI(inventory)

    fun update(backgroundStackSize: Int) {
        inventory.draw(
            listOf("o" * 9,
                "o" + "i".repeat(7) + "o",
                "o" * 9),
            mapOf('o' to fillerItem.asQuantity(backgroundStackSize),
                'i' to head)
        )
    }

    init {
        update(defaultBackgroundStackSize)
        gui.open(player)
        gui.setOnDestroy {
            onClosedByPlayer()
        }
    }

    fun destroy() {
        gui.setOnDestroy {}
        gui.destroy()
        gui.inventory.close()
    }
}

fun showInfoForSeconds(gameName: String, helpLore: List<Component>, player: Player, seconds: Int = 10): CompletableFuture<TaskResult> {
    val cf = CompletableFuture<TaskResult>()
    var countdown: TickingCountdown? = null

    val disp = InfoDisplay(gameName.mm(), helpLore, player, seconds) {
        countdown?.cancel()
        cf.complete(TaskResult.LEFT_TASK)
    }

    countdown = TickingCountdown(
        endAfterSeconds = seconds,
        onTick = { disp.update(-(it.timesRun-seconds)) },
        onTimeout = {
            disp.destroy()
            cf.complete(TaskResult.FINISHED_TASK)
        }
    )

    return cf
}

class ShowInfoUntilCallbackCalled(gameName: Component, helpLore: List<Component>, player: Player) {
    var status = TaskResult.FINISHED_TASK
    private val disp = InfoDisplay(PlainTextComponentSerializer.plainText().serialize(gameName).mm(), helpLore, player, 1) {
        status = TaskResult.LEFT_TASK
    }

    fun close() {
        disp.destroy()
    }
}

enum class TaskResult {
    FINISHED_TASK, LEFT_TASK;

    fun ifFinished(cb: () -> Unit) {
        if (this == FINISHED_TASK) cb()
    }
}
