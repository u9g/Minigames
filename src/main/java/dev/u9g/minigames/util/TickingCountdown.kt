package dev.u9g.minigames.util

import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class TickingCountdown private constructor(endAfterSeconds: Int,
                                           private val secondsBetween: Int,
                                           val onTick: (TickingCountdown) -> Unit,
                                           val onTimeout: () -> Unit) {
    private var isDone = false
    var timesRun = 0
    private var secondsLeft: Double = endAfterSeconds.toDouble()

    init {
        if (!isDone) {
            onTick(this)
            startTicking()
        }
    }

    private fun startTicking() {
        scheduleCallback()
    }

    private fun doNextCallback() {
        scheduleCallback()
    }

    private fun scheduleCallback() = Executors.newSingleThreadScheduledExecutor().schedule({
        if (!isDone && secondsLeft > 0) {
            if (secondsLeft-secondsBetween <= 0) {
                onTimeout()
            } else {
                onTick(this)
                doNextCallback()
            }
        }
        secondsLeft -= secondsBetween
        timesRun++
    }, secondsBetween.toLong(), TimeUnit.SECONDS)

    fun cancel() {
        isDone = true
    }

    companion object {
        fun sync(endAfterSeconds: Int = 10,
                 onTick: (TickingCountdown) -> Unit = {},
                 onTimeout: () -> Unit = {},
                 secondsBetween: Int = 1) = TickingCountdown(endAfterSeconds, secondsBetween, onTick, onTimeout)
    }
}
