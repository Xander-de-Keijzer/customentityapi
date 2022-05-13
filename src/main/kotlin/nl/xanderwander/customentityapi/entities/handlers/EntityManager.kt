package nl.xanderwander.customentityapi.entities.handlers

import net.minecraft.core.Rotations
import nl.xanderwander.customentityapi.Main
import nl.xanderwander.customentityapi.entities.Entity
import nl.xanderwander.customentityapi.entities.Model
import nl.xanderwander.customentityapi.entities.Seat
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.scheduler.BukkitRunnable

class EntityManager: BukkitRunnable() {

    val registry = arrayListOf<Registrable<*>>()

    companion object {

        fun createModel(
            loc: Location,
            name: String = "",
            nameVisible: Boolean = false,
            small: Boolean = false,
            arms: Boolean = false,
            basePlate: Boolean = false,
            marker: Boolean = true,
            visible: Boolean = false,
            glowing: Boolean = false,
            headRotation: Rotations = Rotations(0F, 0F, 0F),
            mainHand: ItemStack = ItemStack(Material.AIR),
            offHand: ItemStack = ItemStack(Material.AIR),
            helmet: ItemStack = ItemStack(Material.AIR),
            chestPlate: ItemStack = ItemStack(Material.AIR),
            leggings: ItemStack = ItemStack(Material.AIR),
            boots: ItemStack = ItemStack(Material.AIR)
        ): Model {
            return Model(
                loc, name, nameVisible, small, arms, basePlate, marker, visible, glowing,
                headRotation, mainHand, offHand, helmet, chestPlate, leggings, boots
            ).register() as Model
        }

        fun createSeat(
            loc: Location,
            name: String = "",
            nameVisible: Boolean = false,
            small: Boolean = false,
            arms: Boolean = false,
            basePlate: Boolean = false,
            marker: Boolean = true,
            visible: Boolean = false,
            glowing: Boolean = false,
        ): Seat {
            return Seat(
                loc, name, nameVisible, small, arms, basePlate, marker, visible, glowing
            ).register() as Seat
        }

        fun createGroup(
            loc: Location,
            vararg entities: Entity
        ): Group {
            return Group(loc).apply {
                addEntities(entities)
                register()
            }
        }

        fun createRegion(
            region: String,
            vararg entities: Entity
        ): Region {
            return Region(region).apply {
                addEntities(entities)
                register()
            }
        }

    }

    override fun run() {
        val players = Bukkit.getOnlinePlayers()

        players.forEach { player ->
            for (registered in registry) {
                when(registered) {
                    is Group  -> update(player, registered, player.location.distanceSquared(registered.loc) < Main.VD_SQR)
                    is Entity -> update(player, registered, player.location.distanceSquared(registered.loc) < Main.VD_SQR)
                    is Region -> {
                        val region = Main.worldGuard.getRegion(registered.region) ?: continue
                        val c = region.contains(player.location.x.toInt(), player.location.y.toInt(), player.location.z.toInt())
                        update(player, registered, c)
                    }
                }
            }
        }

    }

    override fun cancel() {
        registry.forEach { registrable -> registrable.destroy(false) }
        super.cancel()
    }

    private fun update(player: Player, registered: Registrable<*>, condition: Boolean) {
        if (condition) {
            if (!registered.viewers.contains(player)) registered.addViewer(player)
        } else {
            if (registered.viewers.contains(player)) registered.remViewer(player)
        }
    }

}