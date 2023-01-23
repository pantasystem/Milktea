package net.pantasystem.milktea.data.infrastructure.hashtag

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.pantasystem.milktea.api.misskey.hashtag.SearchHashtagRequest
import net.pantasystem.milktea.common.runCancellableCatching
import net.pantasystem.milktea.common.throwIfHasError
import net.pantasystem.milktea.data.api.misskey.MisskeyAPIProvider
import net.pantasystem.milktea.model.hashtag.HashtagRepository
import javax.inject.Inject

class HashtagRepositoryImpl @Inject constructor(
    val misskeyAPIProvider: MisskeyAPIProvider,
): HashtagRepository {
    override suspend fun search(baseUrl: String, query: String, limit: Int, offset: Int): Result<List<String>> = runCancellableCatching{
        withContext(Dispatchers.IO) {

            misskeyAPIProvider.get(baseUrl).searchHashtag(SearchHashtagRequest(
                query = query,
                limit = limit,
                offset = offset
            )).throwIfHasError().body() ?: emptyList()
        }
    }
}