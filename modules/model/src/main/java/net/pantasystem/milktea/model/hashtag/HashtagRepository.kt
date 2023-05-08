package net.pantasystem.milktea.model.hashtag

interface HashtagRepository {
    suspend fun search(accountId: Long, query: String, limit: Int = 10, offset: Int = 0): Result<List<String>>

    suspend fun trends(accountId: Long): Result<List<Hashtag>>
}