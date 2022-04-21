package cafe.ferret.ilokijetesantakalu

import cafe.ferret.ilokijetesantakalu.extensions.StarboardExtension
import com.kotlindiscord.kord.extensions.ExtensibleBot
import com.kotlindiscord.kord.extensions.utils.env
import dev.kord.common.entity.Snowflake

val TEST_SERVER_ID = Snowflake(
    env("TEST_SERVER").toLong()  // Get the test server ID from the env vars or a .env file
)

private val TOKEN = env("TOKEN")   // Get the bot's token from the env vars or a .env file
private val ENVIRONMENT = env("ENVIRONMENT")


suspend fun main() {
    val bot = ExtensibleBot(TOKEN) {
        database(true)
        applicationCommands {
            if (ENVIRONMENT != "production") defaultGuild(TEST_SERVER_ID)
        }
        extensions {
            add(::StarboardExtension)
        }
    }

    bot.start()
}