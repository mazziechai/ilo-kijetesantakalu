package cafe.ferret.ilokijetesantakalu.database.migrations

import cafe.ferret.ilokijetesantakalu.database.collections.ServerConfigCollection
import cafe.ferret.ilokijetesantakalu.database.collections.StarredMessageCollection
import org.litote.kmongo.coroutine.CoroutineDatabase

suspend fun v1(db: CoroutineDatabase) {
    db.createCollection(ServerConfigCollection.name)
    db.createCollection(StarredMessageCollection.name)
}