package dev.u9g.minigames

import dev.u9g.minigames.games.GatheringGame
import dev.u9g.minigames.util.Task
import dev.u9g.minigames.util.TaskResult
import dev.u9g.minigames.util.showRules
import dev.u9g.minigames.util.throwablerenderer.sendToOps
import net.kyori.adventure.text.Component
import net.kyori.adventure.util.Ticks
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import redempt.redlib.inventorygui.InventoryGUI
import redempt.redlib.inventorygui.ItemButton

private val HELP_LORE: List<Component> = listOf(
    "You will be given turns on a board in which you can turn over 2 cards at a time.".mm(),
    "The objective is to flip cards over until you find <gold>two matching cards,</gold>".mm(),
    "then you flip those two cards over and they will be removed from the board.".mm(),
    "Continue doing so until the board is empty, then you have finished your board.".mm()
)
const val SLOTS_IN_CHEST = 27
const val IS_ODD = SLOTS_IN_CHEST % 2 == 1
val HALF_OF_SLOTS_IN_INV = (if (IS_ODD) { SLOTS_IN_CHEST-1 } else { SLOTS_IN_CHEST }) / 2
private val FILLER_MATERIAL = Material.RED_STAINED_GLASS_PANE
private val FILLER_ITEM = makeItem(material = FILLER_MATERIAL, name = " ".mm())
private val USABLE_MATERIALS = Material.values().asList().filter { it !== FILLER_MATERIAL && it !== Material.AIR && it.isItem }

const val WAIT_SECONDS_AFTER_SUCCESSFUL_MATCH = 750.toDouble() / 1000

class MatchingGame private constructor(private val player: Player) {
    private var turns: Int = 0
    private var lastClickedSlot: Int = -1
    private var matchesLeft: Int = HALF_OF_SLOTS_IN_INV
    private val inv: Inventory = Bukkit.createInventory(null, SLOTS_IN_CHEST, "Match em!".mm())
    private val gui: InventoryGUI = InventoryGUI(inv)
    private var waitingForPlayerToSeeVisibleItems = false
    private val hiddenItems = USABLE_MATERIALS.take(HALF_OF_SLOTS_IN_INV)
        .toMutableList().also { it.addAll(it) }.shuffled().shuffled().map { makeItem(material = it) }

    private fun putStatsItem() = inv.setItem(HALF_OF_SLOTS_IN_INV,
        makeItem(material = Material.DIAMOND, name = "<aqua>Turns: </aqua>$turns".mm()))

    private fun hideSlot(slot: Int) = inv.setItem(slot, FILLER_ITEM)

    private fun winningItemForSlot(slot: Int): ItemStack {
        var slotNum = slot
        if (IS_ODD && slot > HALF_OF_SLOTS_IN_INV) slotNum -= 1
        return hiddenItems[slotNum]
    }

    private fun showRealItemInSlot(slot: Int) = inv.setItem(slot, winningItemForSlot(slot))

    private fun startGame() {
        for (slot in 0 until inv.size) {
            if (IS_ODD && slot == HALF_OF_SLOTS_IN_INV) {
                putStatsItem()
                continue
            }
            gui.addButton(ItemButton.create(FILLER_ITEM) { _, _ ->
                if (waitingForPlayerToSeeVisibleItems ||
                    inv.getItem(slot) == null ||
                    lastClickedSlot == slot) return@create
                processClick(slot)
            }, slot)
        }
        gui.open(player)
    }

    private fun processClick(slotClicked: Int) {
        if (IS_ODD && slotClicked == HALF_OF_SLOTS_IN_INV) {
            return
        }
        when (lastClickedSlot) {
            -1 -> onFirstItemClicked(slotClicked)
            else -> onSecondItemClicked(slotClicked)
        }
    }

    private fun onFirstItemClicked(slotClicked: Int) {
        lastClickedSlot = slotClicked
        showRealItemInSlot(slotClicked)
    }

    private fun onSecondItemClicked(slotClicked: Int) {
        showRealItemInSlot(slotClicked)
        waitingForPlayerToSeeVisibleItems = true
        Task.syncDelayed((Ticks.TICKS_PER_SECOND * WAIT_SECONDS_AFTER_SUCCESSFUL_MATCH).toLong()) {
            processSelectedItems(slotClicked)
            waitingForPlayerToSeeVisibleItems = false
            lastClickedSlot = -1
        }
    }

    private fun processSelectedItems(slotClicked: Int) {
        turns++
        if (IS_ODD) putStatsItem()

        if (winningItemForSlot(lastClickedSlot).type == winningItemForSlot(slotClicked).type) {
            onSuccessfulMatch(slotClicked)
        } else {
            onUnsuccessfulMatch(slotClicked)
        }
    }

    private fun onSuccessfulMatch(slotClicked: Int) {
        inv.clear(lastClickedSlot)
        inv.clear(slotClicked)
        if (--matchesLeft == 0) onGameOver()
    }

    private fun onUnsuccessfulMatch(slotClicked: Int) {
        hideSlot(lastClickedSlot)
        hideSlot(slotClicked)
    }

    private fun onGameOver() {
        gui.destroy()
        inv.close()
        player.sendMessage("""It took <green>$turns</green> <aqua>turns</aqua> to find <green>$HALF_OF_SLOTS_IN_INV <aqua>matches</aqua>""".mm())
    }

    init {
        assert(hiddenItems.size == SLOTS_IN_CHEST)
        startGame()
    }

    companion object {
        fun start(player: Player) {
            // TODO: Make Player -> Game Map
            showRules("Matching", HELP_LORE, player)
                .thenAccept { it.ifFinished { MatchingGame(player) } }
                .whenComplete { _, err -> err?.sendToOps() }
        }
    }
}