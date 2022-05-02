package nl.xanderwander.customentityapi.utils

import nl.xanderwander.customentityapi.Main
import org.bukkit.Location
import org.bukkit.scheduler.BukkitRunnable

class Utils {

    companion object {
        fun Location.distSqr(loc: Location): Double {
            return square(this.x - loc.x) + square(this.y - loc.y) + square(this.z - loc.z)
        }

        fun square(a: Double): Double {
            return a * a
        }

        fun runSync(f: () -> Unit) {
            object : BukkitRunnable() {
                override fun run() {
                    f.invoke()
                }
            }.runTask(Main.instance)
        }
    }

}