package nl.xanderwander.customentityapi.entities.handlers

import nl.xanderwander.customentityapi.Main
import nl.xanderwander.customentityapi.entities.Entity
import org.bukkit.entity.Player

class Region(val region: String): Registrable<Region>() {

    init {
        if (!Main.worldGuard.regionExists(region)) {
            Main.instance.logger.warning("Region $region could not be found, any entities using this region will not work.")
        }
    }

    val entities = arrayListOf<Entity>()

    override fun addViewer(player: Player) {
        entities.forEach { entity -> entity.addViewer(player) }
        super.addViewer(player)
    }

    override fun remViewer(player: Player) {
        entities.forEach { entity -> entity.remViewer(player) }
        super.remViewer(player)
    }

    override fun destroy(): Region {
        entities.forEach { entity -> entity.destroy(false) }
        return super.destroy()
    }

}