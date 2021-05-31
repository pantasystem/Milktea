package jp.panta.misskeyandroidclient.model.drive

import android.content.Context
import com.google.gson.Gson
import jp.panta.misskeyandroidclient.api.drive.FilePropertyDTO
import jp.panta.misskeyandroidclient.api.drive.OkHttpDriveFileUploader
import jp.panta.misskeyandroidclient.model.Encryption
import jp.panta.misskeyandroidclient.model.account.Account
import jp.panta.misskeyandroidclient.model.file.File
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import okhttp3.OkHttpClient
import java.lang.IllegalStateException

interface FileUploader {
    @Throws(FileUploadFailedException::class)
    suspend fun upload(file: File, isForce: Boolean): FilePropertyDTO
}

class FileUploadFailedException(val file: File, val throwable: Throwable?, val statusCode: Int?) : IllegalStateException()

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