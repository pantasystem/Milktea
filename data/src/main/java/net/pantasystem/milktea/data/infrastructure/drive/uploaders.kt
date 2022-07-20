package net.pantasystem.milktea.data.infrastructure.drive

import android.content.Context
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.Json
import net.pantasystem.milktea.api.misskey.drive.FilePropertyDTO
import net.pantasystem.milktea.common.Encryption
import net.pantasystem.milktea.model.account.Account
import net.pantasystem.milktea.model.file.AppFile
import okhttp3.OkHttpClient

interface FileUploader {
    @Throws(FileUploadFailedException::class)
    suspend fun upload(file: AppFile.Local, isForce: Boolean): FilePropertyDTO
}

class FileUploadFailedException(val file: AppFile.Local, val throwable: Throwable?, val statusCode: Int?) : IllegalStateException()

interface FileUploaderProvider {
    fun create(account: Account): FileUploader
    fun get(account: Account) : FileUploader
}

class OkHttpFileUploaderProvider(
    val okHttpClient: OkHttpClient,
    val context: Context,
    val json: Json,
    val encryption: Encryption
) : FileUploaderProvider {
    private val lock = Mutex()
    private var instances = mapOf<Long, FileUploader>()

    override fun create(account: Account): FileUploader {
        return runBlocking {
            lock.withLock {
                val map = instances.toMutableMap()
                map[account.accountId] = OkHttpDriveFileUploader(context, account, json, encryption)
                instances = map
                instances[account.accountId]?: throw IllegalStateException("生成したはずのインスタンスが消滅しました！！")
            }
        }
    }

    override fun get(account: Account): FileUploader {
        return instances[account.accountId]?: create(account)
    }

}