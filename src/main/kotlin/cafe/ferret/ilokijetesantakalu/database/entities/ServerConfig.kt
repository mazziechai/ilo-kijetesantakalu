package cafe.ferret.ilokijetesantakalu.database.entities

import cafe.ferret.ilokijetesantakalu.database.Entity
import dev.kord.common.entity.Snowflake
import kotlinx.serialization.Serializable

@Serializable
data class ServerConfig(override val _id: Snowflake, var starboardChannel: Snowflake?, var starsRequired: Int) :
    Entity<Snowflake>
