package rinn.rei.mihane.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class DelayResult(
    val delay: Int = 0,
)
