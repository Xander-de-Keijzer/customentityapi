package nl.xanderwander.customentityapi.entities.handlers

import nl.xanderwander.customentityapi.entities.Entity
import org.bukkit.Location
import org.bukkit.entity.Player

class Group(
    var loc: Location,
    private val entities: ArrayList<Entity> = arrayListOf()
    ): Registrable<Group>() {

    override fun addViewer(player: Player) {
        entities.forEach { entity -> entity.addViewer(player) }
        super.addViewer(player)
    }

    override fun remViewer(player: Player) {
        entities.forEach { entity -> entity.remViewer(player) }
        super.remViewer(player)
    }

    override fun destroy(unregister: Boolean): Group {
        entities.forEach { entity -> entity.destroy(unregister) }
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