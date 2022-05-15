package nl.xanderwander.customentityapi

import net.minecraft.core.Rotations
import nl.xanderwander.customentityapi.entities.Model
import nl.xanderwander.customentityapi.entities.Seat
import nl.xanderwander.customentityapi.entities.handlers.EntityManager
import nl.xanderwander.customentityapi.utils.WorldGuardHook
import nl.xanderwander.customentityapi.protocol.PacketProtocol
import nl.xanderwander.customentityapi.utils.Utils
import org.bukkit.ChatColor
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.java.JavaPlugin
import kotlin.math.cos
import kotlin.math.sin

class Main: JavaPlugin() {

    companion object {
        lateinit var instance: Main
        lateinit var protocol: PacketProtocol
        val entityManager = EntityManager()
        val worldGuard = WorldGuardHook()

        private const val VIEW_DISTANCE = 300
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

            val loc = sender.location.clone()
            val model = Model(loc.clone().add(1.0, 0.0, 0.0), helmet = ItemStack(Material.STONE))
            val model2 = Model(loc.clone().add(1.0, 1.0, 1.0), "test", true, true, true, true, false, true, true, Rotations(0.0F, 90.0F, 0.0F))
            val seat = Seat(loc.clone().add(1.0, 0.0, 2.0), visible = true)

            sender.sendMessage("${ChatColor.BLUE}[CEA] ${ChatColor.GRAY}Entity info:")
            sender.sendMessage("${ChatColor.GRAY} - Entity 1:")
            sender.sendMessage("${ChatColor.GRAY}   -Invisible")
            sender.sendMessage("${ChatColor.GRAY}   -Head: Stone block")
            sender.sendMessage("${ChatColor.GRAY} - Entity 2:")
            sender.sendMessage("${ChatColor.GRAY}   -Visible, Glowing, Small, Base-plate, Arms, Name-visible")
            sender.sendMessage("${ChatColor.GRAY}   -Name: 'test'")
            sender.sendMessage("${ChatColor.GRAY} - Entity 3:")
            sender.sendMessage("${ChatColor.GRAY}   -Rideable")
            sender.sendMessage("${ChatColor.BLUE}[CEA] ${ChatColor.GRAY}Starting test, a stone block should now be visible.")

            val group = EntityManager.createGroup(loc, model)
            group.onlinePlayers = { mutableListOf(sender) }

            Utils.runAfter(200) {
                group.addEntity(model2)
                sender.sendMessage("${ChatColor.BLUE}[CEA] ${ChatColor.GRAY}The second armor stand should now be visible.")
            }

            Utils.runAfter(400) {
                group.addEntity(seat)
                sender.sendMessage("${ChatColor.BLUE}[CEA] ${ChatColor.GRAY}The third rideable stand should now be visible, ${ChatColor.RED}please ride until test completed.")
            }

            Utils.runCounted(600, 1L) { ct ->
                val x = cos(ct.toDouble() / 40) * (ct.toDouble() / 10.0)
                val z = sin(ct.toDouble() / 40) * (ct.toDouble() / 10.0)
                val newLoc = Location(loc.world, loc.x + x + 1, loc.y, loc.z + z + 2)
                seat.moveTo(newLoc)
            }

            Utils.runAfter(800) {
                seat.visible = false
                sender.sendMessage("${ChatColor.BLUE}[CEA] ${ChatColor.GRAY}The third armor stand should now be invisible.")
            }

            Utils.runAfter(2000) {
                group.destroy()
                sender.sendMessage("${ChatColor.BLUE}[CEA] ${ChatColor.GRAY}Test completed, all armor stands should now be destroyed.")
                sender.sendMessage("${ChatColor.GRAY} - All armor stands should only be visible to you")
                sender.sendMessage("${ChatColor.GRAY} - All chunks should load in time, you should not be in empty chunks")
            }

            Utils.runAfter(2001) {
                sender.teleport(loc)
            }

        }

        return true

    }

}