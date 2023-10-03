package net.pantasystem.milktea.common.dhash

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import androidx.core.graphics.drawable.toBitmapOrNull
import java.math.BigInteger

object ImageDHasher {

    private var WINDOW_SIZE = 8
    fun hashFromBitmap(bitmap: Bitmap?): String? {
        bitmap ?: return null
        val scaledBitmap = Bitmap.createScaledBitmap(
            bitmap, WINDOW_SIZE + 1, WINDOW_SIZE + 1, true
        )
        val imageDimention = WINDOW_SIZE + 1
        val pixels = IntArray(imageDimention * imageDimention)
        scaledBitmap.getPixels(pixels, 0, imageDimention, 0, 0, imageDimention, imageDimention)

        //generate the D-Hash
        val dHash = generateHash(pixels)

        //clean up
        scaledBitmap.recycle()
        return dHash?.toString()
    }

    fun hashFromDrawable(drawable: Drawable): String? {
        val bitmap = drawable.toBitmapOrNull(WINDOW_SIZE + 1, WINDOW_SIZE + 1)
            ?: return null
        return hashFromBitmap(bitmap)
    }

    fun hashFromPath(path: String?): String? {
        val bmOptions = BitmapFactory.Options()
        bmOptions.inDither = false
        val bitmap = BitmapFactory.decodeFile(path, bmOptions)
        val scaledBitmap =
            Bitmap.createScaledBitmap(bitmap, WINDOW_SIZE + 1, WINDOW_SIZE + 1, true)
                ?: return null
        val imageDimention = WINDOW_SIZE + 1
        val pixels = IntArray(imageDimention * imageDimention)
        scaledBitmap.getPixels(pixels, 0, imageDimention, 0, 0, imageDimention, imageDimention)

        //generate the D-Hash
        val dHash = generateHash(pixels)

        //clean up
        bitmap.recycle()
        scaledBitmap.recycle()
        return dHash?.toString()
    }

    private fun generateHash(pixels: IntArray?): BigInteger? {
        var count = 0
        var result = BigInteger("0")
        if (pixels == null || pixels.size == 0) {
            return null
        }
        for (y in 0 until WINDOW_SIZE) {
            for (x in 0 until WINDOW_SIZE) {
                var index = y * WINDOW_SIZE + x
                var R = pixels[index] shr 16 and 0xff
                var G = pixels[index] shr 8 and 0xff
                var B = pixels[index] and 0xff
                val greyLeft = (R + G + B) / 3
                index = y * WINDOW_SIZE + (x + 1)
                R = pixels[index] shr 16 and 0xff
                G = pixels[index] shr 8 and 0xff
                B = pixels[index] and 0xff
                val greyRight = (R + G + B) / 3
                if (greyLeft > greyRight) {
                    var shifted = BigInteger("1")
                    shifted = shifted.shiftLeft(count)
                    result = result.or(shifted)
                }
                count++
            }
        }
        return result
    }
}