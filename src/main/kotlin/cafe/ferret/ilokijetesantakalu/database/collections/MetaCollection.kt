package cafe.ferret.ilokijetesantakalu.database.collections

import cafe.ferret.ilokijetesantakalu.database.Collection
import cafe.ferret.ilokijetesantakalu.database.Database
import cafe.ferret.ilokijetesantakalu.database.entities.Meta
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class MetaCollection : KoinComponent {
    private val database: Database by inject()
    private val col = database.mongo.getCollection<Meta>(name)

    suspend fun get() = col.findOne()
    suspend fun set(meta: Meta) = col.save(meta)

    companion object : Collection("meta")
}