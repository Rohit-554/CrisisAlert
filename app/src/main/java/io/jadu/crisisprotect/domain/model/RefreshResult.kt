package io.jadu.crisisprotect.domain.model

sealed interface RefreshResult {
    data object Success : RefreshResult
    data object Failure : RefreshResult
}
