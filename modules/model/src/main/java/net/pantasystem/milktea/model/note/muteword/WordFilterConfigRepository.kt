package net.pantasystem.milktea.model.note.muteword

import kotlinx.coroutines.flow.Flow


interface WordFilterConfigRepository {
    suspend fun save(config: WordFilterConfig): Result<Unit>

    suspend fun get(): Result<WordFilterConfig>

    fun observe(): Flow<WordFilterConfig>
}