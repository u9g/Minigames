package dev.u9g.minigames

import org.bukkit.Bukkit
import org.bukkit.plugin.Plugin
import java.util.function.Consumer

/**
 * Simple utility for Bukkit scheduler tasks, essentially just shorthand
 * @author Redempt
 */
class Task private constructor(
    private val task: Int,
    /**
     * @return The type of this Task
     */
    val type: TaskType,
    /**
     * @return The Plugin which scheduled this task
     */
    val plugin: Plugin
) {

    /**
     * @return Whether this Task is queued, same as [org.bukkit.scheduler.BukkitScheduler.isQueued]
     */
    val isQueued: Boolean
        get() = Bukkit.getScheduler().isQueued(task)

    /**
     * @return Whether this Task is currently running, same as [org.bukkit.scheduler.BukkitScheduler.isCurrentlyRunning]
     */
    val isCurrentlyRunning: Boolean
        get() = Bukkit.getScheduler().isCurrentlyRunning(task)

    /**
     * Cancels this task, same as [org.bukkit.scheduler.BukkitScheduler.cancelTask]
     */
    fun cancel() {
        Bukkit.getScheduler().cancelTask(task)
    }

    /**
     * Represents a type of task
     */
    enum class TaskType {
        SYNC_DELAYED, ASYNC_DELAYED, SYNC_REPEATING, ASYNC_REPEATING
    }

    companion object {

        /**
         * Schedules a sync delayed task to run as soon as possible
         * @param run The task to run
         * @return The Task that has been scheduled
         */
        fun syncDelayed(run: Consumer<Task?>): Task? {
            return syncDelayed(0, run)
        }

        /**
         * Schedules a sync delayed task to run after a delay
         * @param run The task to run
         * @param delay The delay in ticks to wait before running the task
         * @return The Task that has been scheduled
         */
        fun syncDelayed(delay: Long, run: Consumer<Task?>): Task? {
            return syncDelayed(getCallingPlugin(), delay, run)
        }
        /**
         * Schedules a sync delayed task to run after a delay
         * @param plugin The plugin scheduling the task
         * @param run The task to run
         * @param delay The delay in ticks to wait before running the task
         * @return The Task that has been scheduled
         */
        /**
         * Schedules a sync delayed task to run as soon as possible
         * @param plugin The plugin scheduling the task
         * @param run The task to run
         * @return The Task that has been scheduled
         */
        private fun syncDelayed(plugin: Plugin, delay: Long = 0, run: Consumer<Task?>): Task? {
            val task = arrayOf<Task?>(null)
            task[0] = Task(Bukkit.getScheduler().scheduleSyncDelayedTask(plugin,
                { run.accept(task[0]) }, delay
            ), TaskType.SYNC_DELAYED, plugin)
            return task[0]
        }

        /**
         * Schedules a sync repeating task to run later
         * @param run The task to run
         * @param delay The delay in ticks to wait before running the task
         * @param period The number of ticks between executions of the task
         * @return The Task that has been scheduled
         */
        fun syncRepeating(delay: Long, period: Long, run: Consumer<Task?>): Task? {
            return syncRepeating(getCallingPlugin(), delay, period, run)
        }

        /**
         * Schedules a sync repeating task to run later
         * @param plugin The plugin scheduling the task
         * @param run The task to run
         * @param delay The delay in ticks to wait before running the task
         * @param period The number of ticks between executions of the task
         * @return The Task that has been scheduled
         */
        private fun syncRepeating(plugin: Plugin, delay: Long, period: Long, run: Consumer<Task?>): Task? {
            val task = arrayOf<Task?>(null)
            task[0] = Task(Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin,
                { run.accept(task[0]) }, delay, period
            ), TaskType.SYNC_REPEATING, plugin)
            return task[0]
        }
        /**
         * Schedules an async delayed task to run after a delay
         * @param run The task to run
         * @param delay The delay in ticks to wait before running the task
         * @return The Task that has been scheduled
         */
        fun asyncDelayed(delay: Long, run: Consumer<Task?>): Task? {
            return asyncDelayed(getCallingPlugin(), delay, run)
        }
        /**
         * Schedules an async delayed task to run after a delay
         * @param plugin The plugin scheduling the task
         * @param run The taske eto run
         * @param delay The delay in ticks to wait before running the task
         * @return The Task that has been scheduled
         */
        /**
         * Schedules an async delayed task to run as soon as possible
         * @param plugin The plugin scheduling the task
         * @param run The task to run
         * @return The Task that has been scheduled
         */
        private fun asyncDelayed(plugin: Plugin, delay: Long = 0, run: Consumer<Task?>): Task? {
            val task = arrayOf<Task?>(null)
            task[0] = Task(Bukkit.getScheduler().scheduleAsyncDelayedTask(plugin,
                { run.accept(task[0]) }, delay ), TaskType.ASYNC_DELAYED, plugin)
            return task[0]
        }

        /**
         * Schedules an async repeating task to run later
         * @param run The task to run
         * @param delay The delay in ticks to wait before running the task
         * @param period The number of ticks between executions of the task
         * @return The Task that has been scheduled
         */
        fun asyncRepeating(delay: Long, period: Long, run: Consumer<Task?>): Task? {
            return asyncRepeating(getCallingPlugin(), delay, period, run)
        }

        /**
         * Schedules an async repeating task to run later
         * @param plugin The plugin scheduling the task
         * @param run The task to run
         * @param delay The delay in ticks to wait before running the task
         * @param period The number of ticks between executions of the task
         * @return The Task that has been scheduled
         */
        private fun asyncRepeating(plugin: Plugin, delay: Long, period: Long, run: Consumer<Task?>): Task? {
            val task = arrayOf<Task?>(null)
            task[0] = Task(Bukkit.getScheduler().scheduleAsyncRepeatingTask(plugin,
                { run.accept(task[0]) }, delay, period
            ), TaskType.ASYNC_REPEATING, plugin)
            return task[0]
        }
    }
}