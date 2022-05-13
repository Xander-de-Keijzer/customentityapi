package nl.xanderwander.customentityapi

import nl.xanderwander.customentityapi.entities.Model
import nl.xanderwander.customentityapi.entities.Seat
import nl.xanderwander.customentityapi.entities.handlers.EntityManager
import nl.xanderwander.customentityapi.utils.WorldGuardHook
import nl.xanderwander.customentityapi.protocol.PacketProtocol
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.plugin.java.JavaPlugin

class Main: JavaPlugin() {

    companion object {
        lateinit var instance: Main
        lateinit var protocol: PacketProtocol
        val entityManager = EntityManager()
        val worldGuard = WorldGuardHook()

        private const val VIEW_DISTANCE = 100
        const val VD_SQR = VIEW_DISTANCE * VIEW_DISTANCE
    }

    override fun onEnable() {

        instance = this
        protocol = PacketProtocol(instance)
        entityManager.runTaskTimer(instance, 0L, 1L)
        worldGuard.load()

        registerCommands()
        registerEvents()

        logger.info("${description.name} V${description.version} has been enabled.")

    }

    override fun onDisable() {

        entityManager.cancel()
        protocol.close()

        logger.info("${description.name} has been disabled.")

    }

    private fun registerEvents() {
        //Bukkit.getPluginManager().registerEvents(, this)
    }

    private fun registerCommands() {
        description.commands.keys.forEach { name -> getCommand(name)?.setExecutor(this) }
    }

}