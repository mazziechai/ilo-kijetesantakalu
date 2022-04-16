package cafe.ferret.ilokijetesantakalu.database.collections

import cafe.ferret.ilokijetesantakalu.database.Collection
import cafe.ferret.ilokijetesantakalu.database.Database
import cafe.ferret.ilokijetesantakalu.database.entities.ServerConfig
import dev.kord.common.entity.Snowflake
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.litote.kmongo.eq

class ServerConfigCollection : KoinComponent {
    private val database: Database by inject()
    private val col = database.mongo.getCollection<ServerConfig>(name)

    suspend fun new(id: Snowflake, channel: Snowflake?, starsRequired: Int = 5) =
        set(ServerConfig(id, channel, starsRequired))

    suspend fun get(id: Snowflake) = col.findOne(ServerConfig::_id eq id)
    suspend fun set(config: ServerConfig) = col.save(config)

    companion object : Collection("configs")
}