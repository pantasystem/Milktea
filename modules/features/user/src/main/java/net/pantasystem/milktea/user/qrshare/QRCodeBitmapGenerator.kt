package net.pantasystem.milktea.user.qrshare

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import com.bumptech.glide.Glide
import com.bumptech.glide.request.FutureTarget
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.glxn.qrgen.android.QRCode
import net.pantasystem.milktea.common.runCancellableCatching
import net.pantasystem.milktea.model.account.AccountRepository
import net.pantasystem.milktea.model.user.User
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine


class QRCodeBitmapGenerator @Inject constructor(
    @ApplicationContext private val context: Context,
    private val accountRepository: AccountRepository,
) {
    suspend operator fun invoke(
        user: User,
        size: Int,
    ): Result<Bitmap?> = runCancellableCatching {
        val qrCode = QRCode.from(
            user.getProfileUrl(accountRepository.get(user.id.accountId).getOrThrow()),
        ).withSize(size, size).bitmap()
            ?: return@runCancellableCatching null

        val avatarIcon = user.avatarUrl?.let {
            withContext(Dispatchers.IO) {
                loadProfileGlide(it, size / 8)
            }
        }
        overlayBitmapsCentered(avatarIcon, qrCode)
    }


    private fun overlayBitmapsCentered(avatarIcon: Bitmap?, qrCode: Bitmap): Bitmap {
        // 重ねるための新しいBitmapを生成
        val result = Bitmap.createBitmap(
            qrCode.width,
            qrCode.height,
            qrCode.config
        )

        val canvas = Canvas(result)
        canvas.drawBitmap(qrCode, 0f, 0f, null)

        if (avatarIcon != null) {
            val left = (qrCode.width - avatarIcon.width) / 2.0f
            val top = (qrCode.height - avatarIcon.height) / 2.0f

            canvas.drawBitmap(avatarIcon, left, top, null)
        }
        // Bitmap AをBitmap Bの中央に描画するための座標を計算


        return result
    }

    private suspend fun loadProfileGlide(
        avatarUrl: String,
        size: Int,
    ): Bitmap? {
        return suspendCoroutine<Bitmap?> { continuation ->
            val futureTarget: FutureTarget<Bitmap> = Glide
                .with(context)
                .asBitmap()
                .circleCrop()
                .override(size)
                .load(avatarUrl)
                .submit()

            try {
                val bitmap = futureTarget.get()  // この呼び出しはブロックされ、ロードが完了するまで待機します
                continuation.resume(bitmap)
            } catch (e: Exception) {
                continuation.resume(null)  // エラーが発生した場合、nullを返す
            } finally {
                Glide.with(context).clear(futureTarget)  // 必ずクリアすること
            }
        }
    }
}