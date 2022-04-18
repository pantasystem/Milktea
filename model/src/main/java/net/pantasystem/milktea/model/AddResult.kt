package net.pantasystem.milktea.model

/**
 * Repositoryなどでaddした時の結果の戻り値
 */
enum class AddResult {
    UPDATED,
    CREATED,
    CANCEL
}