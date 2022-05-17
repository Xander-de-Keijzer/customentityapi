package nl.xanderwander.customentityapi.entities.handlers

import net.minecraft.network.protocol.game.ClientboundClearTitlesPacket
import nl.xanderwander.customentityapi.Main
import nl.xanderwander.customentityapi.entities.Entity
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitRunnable

class EntityManager: BukkitRunnable() {

    val registry = arrayListOf<Registrable<*>>()

    companion object {

        fun createGroup(
            loc: Location,
            vararg entities: Entity
        ): Group {
            return Group(loc).apply {
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
                    is Group  -> update(player, registered, player.location.distanceSquared(registered.getLocation()) < Main.VD_SQR)
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