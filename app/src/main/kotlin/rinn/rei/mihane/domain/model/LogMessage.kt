package rinn.rei.mihane.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class LogMessage(
    val type: String = "",
    val payload: String = "",
)
