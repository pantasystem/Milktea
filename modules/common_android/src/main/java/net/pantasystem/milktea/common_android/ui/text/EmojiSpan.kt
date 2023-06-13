package net.pantasystem.milktea.common_android.ui.text

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.drawable.Drawable
import android.text.TextPaint
import android.text.style.ReplacementSpan
import kotlin.math.min

/**
 * @param key 画像の種別を識別するためのキー値で、画像のURLなどが入る
 * @param aspectRatio 画像の比率が入る
 */
abstract class EmojiSpan<T: Any?>(val key: T, val aspectRatio: Float? = null) : ReplacementSpan(){

    companion object {
        /**
         * 変数keyに対応するDrawableの画像サイズをここに保持している。
         */
        private val drawableSizeCache = mutableMapOf<Any, EmojiSizeCache>()
    }

    var imageDrawable: Drawable? = null

    /**
     * imageDrawableにDrawableが代入されている時にupdateImageDrawableSizeが呼び出されるとここに絵文字のサイズが代入される。
     * 画像は縦横比が異なることがあるので、それぞれの高さが代入される。
     */
    private var textHeight: Int = 0
    private var textWidth: Int = 0
    private var isSizeComputed = false

    /**
     * imageDrawableがnullの時にupdateImageDrawableSizeが呼び出されるとここに絵文字のサイズが代入される
     * またDrawableがNullの段階の時にupdateImageDrawableSizeが呼び出された時は画像が正方形として扱われる。
     */
    private var beforeTextSize: Int = 0


    override fun getSize(
        paint: Paint,
        text: CharSequence?,
        start: Int,
        end: Int,
        fm: Paint.FontMetricsInt?
    ): Int {
        val textHeight = paint.textSize

        val drawable = imageDrawable
        val size = key?.let {
            drawableSizeCache[key]
        } ?: drawable?.let {
            // NOTE: drawableSizeCacheに画像のサイズが登録されていない場合は、drawableからサイズを取得する
            EmojiSizeCache(
                intrinsicHeight = it.intrinsicHeight,
                intrinsicWidth = it.intrinsicWidth
            )
        } ?: aspectRatio?.let {
            // NOTE: drawableが読み込まれていない状態の時は、文字の高さと画像の比率から横幅のサイズを取得する
            EmojiSizeCache(
                intrinsicHeight = textHeight.toInt(),
                intrinsicWidth = (textHeight * aspectRatio).toInt()
            )
        }

        // NOTE: keyが存在しかつdrawableが存在する場合は、drawableSizeCacheを更新する
        key?.run {
            drawableSizeCache[key] ?: drawable?.let {
                EmojiSizeCache(
                    intrinsicHeight = it.intrinsicHeight,
                    intrinsicWidth = it.intrinsicWidth
                )
            }
        }
        val metrics = paint.fontMetricsInt
        if (fm != null) {
            fm.top = metrics.top
            fm.ascent = metrics.ascent
            fm.descent = metrics.descent
            fm.bottom = metrics.bottom
        }

        // NOTE: 画像のサイズが不明かつ初めてサイズを取得しようとした時は暫定的なサイズを返す
        if (size == null || beforeTextSize != 0) {
            beforeTextSize = (paint.textSize * 1.2).toInt()
            return beforeTextSize
        }
        key?.run {
            drawableSizeCache[key] = size
        }

        // NOTE: 暫定的なサイズではない場合はbeforeTextSizeを0にする必要性がある
        beforeTextSize = 0

        val imageWidth = size.intrinsicWidth
        val imageHeight = size.intrinsicHeight

        // 画像がテキストの高さよりも大きい場合、画像をテキストと同じ高さに縮小する
        val scale = textHeight / imageHeight.toFloat()
        // テキストの高さに合わせた画像の幅
        return (imageWidth * scale).toInt()
    }

    override fun updateDrawState(ds: TextPaint) {
        super.updateDrawState(ds)
        updateImageDrawableSize(ds)
    }


    override fun draw(
        canvas: Canvas,
        text: CharSequence?,
        start: Int,
        end: Int,
        x: Float,
        top: Int,
        y: Int,
        bottom: Int,
        paint: Paint
    ) {
        val drawable = imageDrawable
        drawable?: return

        canvas.save()
        updateImageDrawableSize(paint)
        var transY = (bottom - drawable.bounds.bottom).toFloat()
        transY -= paint.fontMetrics.descent / 2
        canvas.translate(x, transY)
        drawable.draw(canvas)
        canvas.restore()

    }


    private fun updateImageDrawableSize(paint: Paint) {
        val emojiHeight = min((paint.textSize).toInt(), 128)
        val drawable = imageDrawable
        val size = key?.let {
            drawableSizeCache[key]
        } ?: drawable?.let {
            EmojiSizeCache(
                intrinsicWidth = it.intrinsicWidth,
                intrinsicHeight = it.intrinsicHeight
            )
        } ?: aspectRatio?.let {
            EmojiSizeCache(
                intrinsicHeight = emojiHeight,
                intrinsicWidth = (emojiHeight.toFloat() * aspectRatio).toInt()
            )
        } ?: return
        key?.run {
            drawableSizeCache[key] = size
        }
        val imageWidth = size.intrinsicWidth
        val imageHeight = size.intrinsicHeight

        val unknownEmojiSize = imageWidth <= 0 || imageHeight <= 0
        if (beforeTextSize != 0 && beforeTextSize != emojiHeight || unknownEmojiSize) {
            if (!isSizeComputed) {
                beforeTextSize = emojiHeight
                imageDrawable?.setBounds(0, 0, emojiHeight, emojiHeight)
                isSizeComputed = imageDrawable != null
            }
            return
        }

        val ratio = imageWidth.toFloat() / imageHeight.toFloat()

        val scaledImageWidth = (emojiHeight * ratio).toInt()

        if (!isSizeComputed) {
            textHeight = emojiHeight
            textWidth = scaledImageWidth
            isSizeComputed = imageDrawable != null
            imageDrawable?.setBounds(0, 0, scaledImageWidth, emojiHeight)
        }
    }

}

data class EmojiSizeCache(
    val intrinsicWidth: Int,
    val intrinsicHeight: Int,
)