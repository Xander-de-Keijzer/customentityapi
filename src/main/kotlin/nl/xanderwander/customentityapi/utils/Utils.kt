package nl.xanderwander.customentityapi.utils

import nl.xanderwander.customentityapi.Main
import org.bukkit.scheduler.BukkitRunnable

class Utils {

    companion object {
        fun runSync(f: () -> Unit) {
            object : BukkitRunnable() {
                override fun run() {
                    f.invoke()
                }
            }.runTask(Main.instance)
        }
    }

}