package site.remlit.blueb.revenge.util.inline

import org.bukkit.Bukkit
import site.remlit.blueb.revenge.Revenge

/**
 * Uses Bukkit scheduler to run task later
 *
 * @param tickDelay Delay in ticks
 * */
inline fun scheduled(tickDelay: Long = 0, crossinline block: () -> Unit) =
    Bukkit.getScheduler().runTaskLater(
        Revenge.instance,
        Runnable { block() },
        tickDelay
    )

/**
 * Uses Bukkit scheduler to run task later asynchronously
 *
 * @param tickDelay Delay in ticks
 * */
inline fun scheduledAsync(tickDelay: Long = 0, crossinline block: () -> Unit) =
    Bukkit.getScheduler().runTaskLaterAsynchronously(
        Revenge.instance,
        Runnable { block() },
        tickDelay
    )