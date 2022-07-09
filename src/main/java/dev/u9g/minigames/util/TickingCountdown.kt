package dev.u9g.minigames.util

import net.kyori.adventure.util.Ticks
import org.bukkit.scheduler.BukkitRunnable
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class TickingCountdown constructor(
    endAfterSeconds: Int = 10,
    private val secondsBetween: Int = 1,
    private val onTick: (TickingCountdown) -> Unit = {},
    private val onTimeout: () -> Unit = {},
    private val runOnMain: Boolean = true) {

    private var isDone = false
    var timesRun = 0
    private var secondsLeft: Double = endAfterSeconds.toDouble()

    init {
        if (!isDone) runCallbackAfter(0)
    }

    private fun runInCallback() {
        if (!isDone && secondsLeft > 0) {
            secondsLeft -= secondsBetween
            timesRun++
            if (secondsLeft <= 0) {
                onTimeout()
            } else {
                onTick(this)
            }
            runCallbackAfter(secondsBetween)
        }
    }

    private fun runCallbackAfter(seconds: Int) {
        if (runOnMain) {
            object : BukkitRunnable() {
                override fun run() {
                    try {
                        runInCallback()
                    } catch (ex: Exception) {
                        ex.printStackTrace()
                    }
                }
            }.runTaskLater(getCallingPlugin(), (Ticks.TICKS_PER_SECOND * seconds).toLong())
        } else {
            Executors.newSingleThreadScheduledExecutor()
                .schedule({ runInCallback() }, seconds.toLong(), TimeUnit.SECONDS)
        }
    }

    fun cancel() {
        isDone = true
    }
}
