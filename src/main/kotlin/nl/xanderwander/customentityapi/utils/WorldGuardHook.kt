package nl.xanderwander.customentityapi.utils

import com.sk89q.worldguard.protection.regions.ProtectedRegion
import com.sk89q.worldguard.protection.regions.RegionContainer
import nl.xanderwander.customentityapi.Main
import protocol.Reflection

class WorldGuardHook {

    var regionContainer: RegionContainer? = null

    fun regionExists(name: String): Boolean {
        return getRegion(name) != null
    }

    fun getRegion(name: String): ProtectedRegion? {
        val container = regionContainer ?: return null
        for (regionManager in container.loaded) {
            return (regionManager.getRegion(name) ?: continue)
        }
        return null
    }

    fun load() {

        try {
            val getInstance = Reflection.getMethod("WorldGuard", "getInstance")
            val worldGuardInstance = getInstance.invoke(null)
            val worldGuard = worldGuardInstance as com.sk89q.worldguard.WorldGuard
            regionContainer = worldGuard.platform.regionContainer
        } catch (e: Exception) {
            Main.instance.logger.warning("Could not load the WorldGuard instance.")
        }

    }

}