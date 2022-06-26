package dev.u9g.minigames

import com.github.u9g.u9gutils.ItemBuilder
import net.kyori.adventure.text.Component
import net.kyori.adventure.util.Ticks
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import redempt.redlib.inventorygui.InventoryGUI
import redempt.redlib.inventorygui.ItemButton
import java.util.concurrent.CompletableFuture

private val RULES_INVENTORY_NAME = "Matching Game Rules".mm()
private val HELP_LORE: List<Component> = listOf(
    "You will be given turns on a board in which you can turn over 2 cards at a time.".mm(),
    "The objective is to flip cards over until you find <gold>two matching cards,</gold>".mm(),
    "then you flip those two cards over and they will be removed from the board.".mm(),
    "Continue doing so until the board is empty, then you have finished your board.".mm()
)

private enum class HEADS(private val headTexture: String) {
    R("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYTVjZWQ5OTMxYWNlMjNhZmMzNTEzNzEzNzliZjA1YzYzNWFkMTg2OTQzYmMxMzY0NzRlNGU1MTU2YzRjMzcifX19"),
    U("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNjA3ZmJjMzM5ZmYyNDFhYzNkNjYxOWJjYjY4MjUzZGZjM2M5ODc4MmJhZjNmMWY0ZWZkYjk1NGY5YzI2In19fQ=="),
    L("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMzE5ZjUwYjQzMmQ4NjhhZTM1OGUxNmY2MmVjMjZmMzU0MzdhZWI5NDkyYmNlMTM1NmM5YWE2YmIxOWEzODYifX19"),
    E("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZGJiMjczN2VjYmY5MTBlZmUzYjI2N2RiN2Q0YjMyN2YzNjBhYmM3MzJjNzdiZDBlNGVmZjFkNTEwY2RlZiJ9fX0="),
    S("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvM2U0MWM2MDU3MmM1MzNlOTNjYTQyMTIyODkyOWU1NGQ2Yzg1NjUyOTQ1OTI0OWMyNWMzMmJhMzNhMWIxNTE3In19fQ==");

    fun getHead(): ItemStack = ItemBuilder.of(Material.PLAYER_HEAD).setHeadSkin(headTexture).name(RULES_INVENTORY_NAME).lore(
        HELP_LORE).build()

    operator fun invoke(): ItemStack {
        return this.getHead()
    }
}

private fun makeFillerItem(stackSize: Int) = ItemBuilder.of(Material.WHITE_STAINED_GLASS_PANE).count(stackSize).build()

private fun updateHelpInventory(inventory: Inventory, bgItem: ItemStack) = inventory.draw(
    listOf("o" * 9,
        "o"*2 + "R" + "U" + "L" + "E" + "S" + "o"*2,
        "o" * 9),
    mapOf('o' to bgItem,
        'R' to HEADS.R(),
        'U' to HEADS.U(),
        'L' to HEADS.L(),
        'E' to HEADS.E(),
        'S' to HEADS.S())
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
    private val inv: Inventory = Bukkit.createInventory(null, SLOTS_IN_CHEST, RULES_INVENTORY_NAME)
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
        private fun showRules(player: Player): CompletableFuture<Void> {
            val cf = CompletableFuture<Void>()
            val rulesInventory = Bukkit.createInventory(null, 27, RULES_INVENTORY_NAME)
            val rulesGUI = InventoryGUI(rulesInventory)
            rulesGUI.open(player)
            val countdown = TickingCountdown.sync(
                endAfterSeconds = 10,
                onTick = { runSync { updateHelpInventory(rulesInventory, makeFillerItem(-(it.timesRun-10))) } },
                onTimeout = { runSync { rulesGUI.destroy().apply { cf.complete(null) } } }
            )
            rulesGUI.setOnDestroy { countdown.cancel() }
            return cf
        }

        fun start(player: Player) {
            showRules(player).thenAccept {
                // TODO: Make a player -> Game map
                MatchingGame(player)
            }
        }
    }
}