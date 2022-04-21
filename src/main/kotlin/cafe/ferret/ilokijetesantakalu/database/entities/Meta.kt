package cafe.ferret.ilokijetesantakalu.database.entities

import cafe.ferret.ilokijetesantakalu.database.Entity

data class Meta(val version: Int, override val _id: String = "meta") : Entity<String>
