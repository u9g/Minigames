package dev.u9g.minigames.games

import dev.u9g.minigames.makeItem
import dev.u9g.minigames.util.GameState
import dev.u9g.minigames.util.Task
import dev.u9g.minigames.util.infodisplay.InfoDisplayer
import dev.u9g.minigames.util.infodisplay.TaskResult
import dev.u9g.minigames.util.mm
import dev.u9g.minigames.util.throwablerenderer.sendToOps
import net.kyori.adventure.util.Ticks
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.block.data.type.Leaves
import org.bukkit.block.data.type.Sapling
import org.bukkit.entity.Player
import org.bukkit.inventory.*
import redempt.redlib.inventorygui.InventoryGUI
import redempt.redlib.inventorygui.ItemButton

val GAME_INFO_LORE = listOf(
        "You will be given turns on a board in which you can turn over 2 cards at a time.".mm(),
        "The objective is to flip cards over until you find <gold>two matching cards,</gold>".mm(),
        "then you flip those two cards over and they will be removed from the board.".mm(),
        "Continue doing so until the board is empty, then you have finished your board.".mm()
)

const val SLOTS_IN_CHEST = 27
const val INVENTORY_HAS_ODD_SLOT_NUM = SLOTS_IN_CHEST % 2 == 1
val HALF_OF_SLOTS_IN_INV = (if (INVENTORY_HAS_ODD_SLOT_NUM) { SLOTS_IN_CHEST -1 } else { SLOTS_IN_CHEST }) / 2
private val FILLER_MATERIAL = Material.RED_STAINED_GLASS_PANE
private val FILLER_ITEM = makeItem(material = FILLER_MATERIAL, name = " ".mm())
private val USABLE_MATERIALS = Material.values()
        .asList().filter { it !== FILLER_MATERIAL && it !== Material.AIR && it.isItem &&
        !Bukkit.getRecipesFor(makeItem(material = it)).any { recipe -> recipe is SmithingRecipe || recipe is StonecuttingRecipe } && // way too many cut/polished that look alike
        it.creativeCategory != CreativeCategory.BUILDING_BLOCKS &&
        it.data != Leaves::class.java && it.data != Sapling::class.java }

const val WAIT_SECONDS_AFTER_SUCCESSFUL_MATCH = 750.toDouble() / 1000

class MatchingGame (private val player: Player) : Game {
    private var turns: Int = 0
    private var lastClickedSlot: Int = -1
    private var matchesLeft: Int = HALF_OF_SLOTS_IN_INV
    private val inv: Inventory = Bukkit.createInventory(null, SLOTS_IN_CHEST, "Match em!".mm())
    private val gui: InventoryGUI = InventoryGUI(inv)
    private var waitingForPlayerToSeeVisibleItems = false
    private val hiddenItems = USABLE_MATERIALS.take(HALF_OF_SLOTS_IN_INV)
        .toMutableList().also { it.addAll(it) }.shuffled().shuffled().map { makeItem(material = it) }
    private var gameState: GameState = GameState.STARTING

    private fun putStatsItem() = inv.setItem(
        HALF_OF_SLOTS_IN_INV,
        makeItem(material = Material.DIAMOND, name = "<aqua>Turns: </aqua>$turns".mm())
    )

    private fun hideSlot(slot: Int) = inv.setItem(slot, FILLER_ITEM)

    private fun winningItemForSlot(slot: Int): ItemStack {
        var slotNum = slot
        if (INVENTORY_HAS_ODD_SLOT_NUM && slot > HALF_OF_SLOTS_IN_INV) slotNum -= 1
        return hiddenItems[slotNum]
    }

    private fun showRealItemInSlot(slot: Int) = inv.setItem(slot, winningItemForSlot(slot))

    private fun startGame() {
        for (slot in 0 until inv.size) {
            if (INVENTORY_HAS_ODD_SLOT_NUM && slot == HALF_OF_SLOTS_IN_INV) {
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
        gui.setOnDestroy {
            if (gameState != GameState.OVER) {
                internalEndGame()
            }
        }
    }

    private fun processClick(slotClicked: Int) {
        if (INVENTORY_HAS_ODD_SLOT_NUM && slotClicked == HALF_OF_SLOTS_IN_INV) {
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
        if (INVENTORY_HAS_ODD_SLOT_NUM) putStatsItem()

        if (winningItemForSlot(lastClickedSlot).type == winningItemForSlot(slotClicked).type) {
            onSuccessfulMatch(slotClicked)
        } else {
            onUnsuccessfulMatch(slotClicked)
        }
    }

    private fun onSuccessfulMatch(slotClicked: Int) {
        inv.clear(lastClickedSlot)
        inv.clear(slotClicked)
        matchesLeft--
        if (matchesLeft == 0) onGameOver()
    }

    private fun onUnsuccessfulMatch(slotClicked: Int) {
        hideSlot(lastClickedSlot)
        hideSlot(slotClicked)
    }

    private fun onGameOver() {
        internalEndGame()
        player.sendMessage("It took <green>$turns</green> <aqua>turns</aqua> to find <green>$HALF_OF_SLOTS_IN_INV <aqua>matches</aqua>".mm())
    }

    private fun internalEndGame() {
        gameState = GameState.OVER
        inv.close()
        gui.destroy()
        Games.leftGame(player)
    }

    init {
        assert(hiddenItems.size == SLOTS_IN_CHEST)
    }

    override fun gameState() = gameState

    override suspend fun begin() {
        // TODO: Make Player -> Game Map
        InfoDisplayer.autoclosingDisplay("Matching".mm(), GAME_INFO_LORE, player)
            .thenAccept {
                when (it) {
                    TaskResult.FINISHED_TASK -> {
                        gameState = GameState.IN_PROGRESS
                        startGame()
                    }
                    TaskResult.LEFT_TASK -> {
                        internalEndGame()
                    }
                    null -> throw Error("Game state is null?")
                }
            }
            .whenComplete { _, err -> err?.sendToOps() }
    }

    fun onPlayerLogout() {
        TODO("Not yet implemented")
    }
}