package rinn.rei.mihane.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class MemoryData(
    val inuse: Long = 0,
    val oslimit: Long = 0,
)
