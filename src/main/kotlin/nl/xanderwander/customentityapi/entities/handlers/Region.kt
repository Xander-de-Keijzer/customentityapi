package nl.xanderwander.customentityapi.entities.handlers

import nl.xanderwander.customentityapi.Main
import nl.xanderwander.customentityapi.entities.Entity
import org.bukkit.entity.Player

class Region(
    val region: String,
    private val entities: ArrayList<Entity> = arrayListOf()
): Registrable<Region>() {

    init {
        if (!Main.worldGuard.regionExists(region)) {
            Main.instance.logger.warning("Region $region could not be found, any entities using this region will not work.")
        }
    }

    override fun addViewer(player: Player) {
        entities.forEach { entity -> entity.addViewer(player) }
        super.addViewer(player)
    }

    override fun remViewer(player: Player) {
        entities.forEach { entity -> entity.remViewer(player) }
        super.remViewer(player)
    }

    override fun destroy(unregister: Boolean): Region {
        entities.forEach { entity -> entity.destroy(unregister) }
        if (unregister) unregister()
        return super.destroy(unregister)
    }

    fun addEntity(entity: Entity) {
        entities.add(entity)
        viewers.forEach { viewer -> entity.addViewer(viewer) }
    }

    fun addEntities(entities: Array<out Entity>) {
        entities.forEach { entity -> addEntity(entity) }
    }

    fun remEntity(entity: Entity) {
        entities.remove(entity)
        viewers.forEach { viewer -> entity.remViewer(viewer) }
    }

}