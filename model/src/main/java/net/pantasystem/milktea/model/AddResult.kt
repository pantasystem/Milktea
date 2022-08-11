package net.pantasystem.milktea.model

/**
 * Repositoryなどでaddした時の結果の戻り値
 */
sealed interface AddResult {
    object Updated : AddResult
    object Created : AddResult
    object Canceled : AddResult
}