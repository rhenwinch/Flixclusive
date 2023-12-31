package com.flixclusive.domain.model.provider

import com.flixclusive.providers.interfaces.SourceProvider

data class SourceProviderDetails(
    val source: SourceProvider,
    val isMaintenance: Boolean = false,
    val isIgnored: Boolean = false,
)