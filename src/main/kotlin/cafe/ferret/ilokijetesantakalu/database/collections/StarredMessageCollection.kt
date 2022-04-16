package cafe.ferret.ilokijetesantakalu.database.collections

import cafe.ferret.ilokijetesantakalu.database.Collection
import cafe.ferret.ilokijetesantakalu.database.Database
import cafe.ferret.ilokijetesantakalu.database.entities.StarredMessage
import dev.kord.common.entity.Snowflake
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.litote.kmongo.eq

class StarredMessageCollection : KoinComponent {
    private val database: Database by inject()
    private val col = database.mongo.getCollection<StarredMessage>(name)

    suspend fun new(message: Snowflake, channel: Snowflake): StarredMessage {
        val starredMessage = StarredMessage(message, 1, channel, null)
        set(starredMessage)
        return starredMessage
    }

    suspend fun get(id: Snowflake) = col.findOne(StarredMessage::_id eq id)
    suspend fun set(config: StarredMessage) = col.save(config)

    suspend fun getStarboardMessage(id: Snowflake) = col.findOne(StarredMessage::starboardMessage eq id)

    companion object : Collection("starredMessages")
}