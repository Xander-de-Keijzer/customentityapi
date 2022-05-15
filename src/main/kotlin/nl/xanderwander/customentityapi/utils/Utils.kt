package nl.xanderwander.customentityapi.utils

import nl.xanderwander.customentityapi.Main
import org.bukkit.Location
import org.bukkit.scheduler.BukkitRunnable

class Utils {

    companion object {
        fun Location.distSqr(loc: Location): Double {
            return square(this.x - loc.x) + square(this.y - loc.y) + square(this.z - loc.z)
        }

        private fun square(a: Double): Double {
            return a * a
        }

        fun runSync(f: () -> Unit) {
            object : BukkitRunnable() {
                override fun run() {
                    f.invoke()
                }
            }.runTask(Main.instance)
        }

        fun runAfter(ticks: Long, f: () -> Unit) {
            object : BukkitRunnable() {
                override fun run() {
                    f.invoke()
                }
            }.runTaskLater(Main.instance, ticks)
        }

        fun runCounted(delay: Long, period: Long, f: (Int) -> Unit) {
            var count = 0
            object : BukkitRunnable() {
                override fun run() {
                    f.invoke(count++)
                }
            }.runTaskTimer(Main.instance, delay, period)
        }

    }

}