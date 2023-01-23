package net.pantasystem.milktea.model.hashtag

interface HashtagRepository {
    suspend fun search(baseUrl: String, query: String, limit: Int = 10, offset: Int = 0): Result<List<String>>
}