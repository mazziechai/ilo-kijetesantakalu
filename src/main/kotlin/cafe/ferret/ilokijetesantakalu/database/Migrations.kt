package cafe.ferret.ilokijetesantakalu.database

import cafe.ferret.ilokijetesantakalu.database.collections.MetaCollection
import cafe.ferret.ilokijetesantakalu.database.entities.Meta
import cafe.ferret.ilokijetesantakalu.database.migrations.v1
import mu.KotlinLogging
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

// credit to gdude2002 for writing this lol
object Migrations : KoinComponent {
    private val logger = KotlinLogging.logger { }

    private val db: Database by inject()
    private val metaCollection: MetaCollection by inject()

    suspend fun migrate() {
        var meta = metaCollection.get()

        if (meta == null) {
            meta = Meta(0)

            metaCollection.set(meta)
        }

        var currentVersion = meta.version

        logger.info { "Current database version: v$currentVersion" }

        while (true) {
            val nextVersion = currentVersion + 1

            try {
                when (nextVersion) {
                    1 -> ::v1

                    else -> break
                }(db.mongo)

                logger.info { "Migrated database to v$nextVersion" }
            } catch (t: Throwable) {
                logger.error(t) { "Failed to migrate database to v$nextVersion" }

                throw t
            }

            currentVersion = nextVersion
        }

        if (currentVersion != meta.version) {
            meta = meta.copy(version = currentVersion)

            metaCollection.set(meta)

            logger.info { "Finished database migrations." }
        }
    }
}