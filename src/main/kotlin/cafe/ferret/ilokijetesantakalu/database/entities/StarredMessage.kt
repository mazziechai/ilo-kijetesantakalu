package cafe.ferret.ilokijetesantakalu.database.entities

import cafe.ferret.ilokijetesantakalu.database.Entity
import dev.kord.common.entity.Snowflake
import kotlinx.serialization.Serializable

@Serializable
data class StarredMessage(
    override val _id: Snowflake,
    var stars: Int,
    val channel: Snowflake,
    var starboardMessage: Snowflake?
) : Entity<Snowflake>
