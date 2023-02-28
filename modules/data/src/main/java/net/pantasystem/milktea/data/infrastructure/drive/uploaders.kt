package net.pantasystem.milktea.data.infrastructure.drive

import android.content.Context
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.Json
import net.pantasystem.milktea.api.misskey.OkHttpClientProvider
import net.pantasystem.milktea.data.api.mastodon.MastodonAPIFactory
import net.pantasystem.milktea.data.api.mastodon.MastodonAPIProvider
import net.pantasystem.milktea.data.converters.FilePropertyDTOEntityConverter
import net.pantasystem.milktea.model.account.Account
import net.pantasystem.milktea.model.drive.FileProperty
import net.pantasystem.milktea.model.drive.FilePropertyDataSource
import net.pantasystem.milktea.model.file.AppFile

interface FileUploader {
    @Throws(FileUploadFailedException::class)
    suspend fun upload(file: UploadSource, isForce: Boolean): FileProperty
}

sealed interface UploadSource {
    data class OtherAccountFile(
        val fileProperty: FileProperty
    ) : UploadSource

    data class LocalFile(val file: AppFile.Local) : UploadSource
}

class FileUploadFailedException(
    val file: AppFile,
    val throwable: Throwable?,
    statusCode: Int?,
    errorMessage: String?,
) : IllegalStateException(
    "ファイルアップロードに失敗: file:$file, statusCode:$statusCode, message:$errorMessage",
    throwable
)

interface FileUploaderProvider {
    fun create(account: Account): FileUploader
    fun get(account: Account): FileUploader
}

class OkHttpFileUploaderProvider(
    val okHttpClientProvider: OkHttpClientProvider,
    val context: Context,
    val json: Json,
    val filePropertyDataSource: FilePropertyDataSource,
    private val mastodonAPIFactory: MastodonAPIFactory,
    private val mastodonAPIProvider: MastodonAPIProvider,
    private val filePropertyDTOEntityConverter: FilePropertyDTOEntityConverter,
) : FileUploaderProvider {
    private val lock = Mutex()
    private var instances = mapOf<Long, FileUploader>()

    override fun create(account: Account): FileUploader {
        return runBlocking {
            lock.withLock {
                val map = instances.toMutableMap()
                when (account.instanceType) {
                    Account.InstanceType.MISSKEY -> {
                        map[account.accountId] = MisskeyOkHttpDriveFileUploader(
                            context,
                            account,
                            json,
                            okHttpClientProvider,
                            filePropertyDataSource,
                            filePropertyDTOEntityConverter,
                        )
                        instances = map
                        instances[account.accountId]
                            ?: throw IllegalStateException("生成したはずのインスタンスが消滅しました！！")
                    }
                    Account.InstanceType.MASTODON -> {
                        map[account.accountId] = MastodonOkHttpFileUploader(
                            context,
                            account,
                            json,
                            mastodonAPIFactory,
                            mastodonAPIProvider,
                            filePropertyDataSource
                        )
                        instances = map
                        instances[account.accountId]
                            ?: throw IllegalStateException("生成したはずのインスタンスが消滅しました！！")
                    }
                }

            }
        }
    }

    override fun get(account: Account): FileUploader {
        return instances[account.accountId] ?: create(account)
    }

}