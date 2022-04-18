package net.pantasystem.milktea.data.model.drive

import android.content.Context
import com.google.gson.Gson
import net.pantasystem.milktea.api.misskey.drive.FilePropertyDTO
import net.pantasystem.milktea.data.model.Encryption
import net.pantasystem.milktea.data.model.account.Account
import net.pantasystem.milktea.data.model.file.AppFile
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import okhttp3.OkHttpClient
import java.lang.IllegalStateException

interface FileUploader {
    @Throws(FileUploadFailedException::class)
    suspend fun upload(file: AppFile.Local, isForce: Boolean): net.pantasystem.milktea.api.misskey.drive.FilePropertyDTO
}

class FileUploadFailedException(val file: AppFile.Local, val throwable: Throwable?, val statusCode: Int?) : IllegalStateException()

interface FileUploaderProvider {
    fun create(account: Account): FileUploader
    fun get(account: Account) : FileUploader
}

class OkHttpFileUploaderProvider(
    val okHttpClient: OkHttpClient,
    val context: Context,
    val gson: Gson,
    val encryption: Encryption
) : FileUploaderProvider{
    private val lock = Mutex()
    private var instances = mapOf<Long, FileUploader>()

    override fun create(account: Account): FileUploader {
        return runBlocking {
            lock.withLock {
                val map = instances.toMutableMap()
                map[account.accountId] = OkHttpDriveFileUploader(context, account, gson, encryption)
                instances = map
                instances[account.accountId]?: throw IllegalStateException("生成したはずのインスタンスが消滅しました！！")
            }
        }
    }

    override fun get(account: Account): FileUploader {
        return instances[account.accountId]?: create(account)
    }

}