package nl.xanderwander.customentityapi

import net.minecraft.core.Rotations
import nl.xanderwander.customentityapi.entities.handlers.EntityManager
import nl.xanderwander.customentityapi.utils.WorldGuardHook
import nl.xanderwander.customentityapi.protocol.PacketProtocol
import nl.xanderwander.customentityapi.utils.Utils
import org.bukkit.Material
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
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

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {

        if (sender is Player && args.isNotEmpty() && args[0] == "test") {

            val loc = sender.location
            val model = EntityManager.createModel(loc, helmet = ItemStack(Material.STONE))
            val model2 = EntityManager.createModel(loc.clone().add(0.0, 1.0, 1.0),
                helmet = ItemStack(Material.STONE), visible = true, basePlate = true,
                arms = true, small = true, name = "test", nameVisible = true,
                headRotation = Rotations(0.0f, 1.0f, 0.0f), glowing = true
            )

            Utils.runAfter(100) {
                model.destroy()
                model2.destroy()
            }

        }

        return true

    }

}