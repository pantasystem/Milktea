package net.pantasystem.milktea.data.model

/**
 * Repositoryなどでaddした時の結果の戻り値
 */
enum class AddResult {
    UPDATED,
    CREATED,
    CANCEL
}