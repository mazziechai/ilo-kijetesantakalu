package cafe.ferret.ilokijetesantakalu

import cafe.ferret.ilokijetesantakalu.database.Database
import cafe.ferret.ilokijetesantakalu.database.collections.MetaCollection
import cafe.ferret.ilokijetesantakalu.database.collections.ServerConfigCollection
import cafe.ferret.ilokijetesantakalu.database.collections.StarredMessageCollection
import com.kotlindiscord.kord.extensions.builders.ExtensibleBotBuilder
import com.kotlindiscord.kord.extensions.utils.env
import com.kotlindiscord.kord.extensions.utils.loadModule
import kotlinx.coroutines.runBlocking
import org.koin.dsl.bind


suspend fun ExtensibleBotBuilder.database(migrate: Boolean = false) {
    val uri = env("DB_URI")
    val db = Database(uri)

    hooks {
        beforeKoinSetup {
            loadModule {
                single { db } bind Database::class
            }

            loadModule {
                single { ServerConfigCollection() } bind ServerConfigCollection::class
                single { StarredMessageCollection() } bind StarredMessageCollection::class
                single { MetaCollection() } bind MetaCollection::class
            }

            if (migrate) {
                runBlocking {
                    db.migrate()
                }
            }
        }
    }
}