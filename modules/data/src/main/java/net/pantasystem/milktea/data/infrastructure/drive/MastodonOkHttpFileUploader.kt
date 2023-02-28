package net.pantasystem.milktea.data.infrastructure.drive

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.delay
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import net.pantasystem.milktea.api.mastodon.media.TootMediaAttachment
import net.pantasystem.milktea.api.mastodon.media.UpdateMediaAttachment
import net.pantasystem.milktea.common.throwErrorFromStatusCode
import net.pantasystem.milktea.common.throwIfHasError
import net.pantasystem.milktea.data.api.mastodon.MastodonAPIFactory
import net.pantasystem.milktea.data.api.mastodon.MastodonAPIProvider
import net.pantasystem.milktea.data.infrastructure.toFileProperty
import net.pantasystem.milktea.model.account.Account
import net.pantasystem.milktea.model.drive.FileProperty
import net.pantasystem.milktea.model.drive.FilePropertyDataSource
import okhttp3.MultipartBody
import okhttp3.Request
import java.net.URL

class MastodonOkHttpFileUploader(
    val context: Context,
    val account: Account,
    val json: Json,
    private val mastodonAPIFactory: MastodonAPIFactory,
    private val mastodonAPIProvider: MastodonAPIProvider,
    private val filePropertyDataSource: FilePropertyDataSource,
) : FileUploader {

    override suspend fun upload(file: UploadSource, isForce: Boolean): FileProperty {

        return when (file) {
            is UploadSource.LocalFile -> {
                upload(file)
            }
            is UploadSource.OtherAccountFile -> {
                TODO()
            }
        }

    }

    private suspend fun upload(file: UploadSource.LocalFile): FileProperty {
        val okHttpClient =
            mastodonAPIFactory.getOkHttp(account.normalizedInstanceDomain, account.token)
        val builder = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart(
                "file",
                file.file.name,
                UriRequestBody(Uri.parse(file.file.path), context)
            )
        val request = Request.Builder()
            .url(URL("${account.normalizedInstanceDomain}/api/v2/media"))
            .post(builder.build()).build()
        val response = okHttpClient.newCall(request).execute()
        throwErrorFromStatusCode(response.code)
        val result = json.decodeFromString<TootMediaAttachment>(requireNotNull(response.body?.string()))
        val property = result.toFileProperty(account, false).also {
            filePropertyDataSource.add(it).getOrThrow()
        }
        var code = response.code
        while(code == 202 || code == 206) {
            code = checkUploadStatus(property.id.fileId)
            throwErrorFromStatusCode(code)
            delay(100)
        }
        if (!file.file.comment.isNullOrBlank()) {
            mastodonAPIProvider.get(account).updateMediaAttachment(result.id, UpdateMediaAttachment(
                description = file.file.comment ?: "",
                focus = "0.00,0.00"
            )).throwIfHasError()
        }

        return property
    }

    private fun checkUploadStatus(fileId: String): Int {
        val okHttpClient =
            mastodonAPIFactory.getOkHttp(account.normalizedInstanceDomain, account.token)
        val request = Request.Builder()
            .url(URL("${account.normalizedInstanceDomain}/api/v1/media/${fileId}"))
            .get()
            .build()
        return okHttpClient.newCall(
            request
        ).execute().code
    }
}