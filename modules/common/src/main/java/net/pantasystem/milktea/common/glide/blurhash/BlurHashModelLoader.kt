package net.pantasystem.milktea.common.glide.blurhash

import android.content.Context
import android.graphics.drawable.BitmapDrawable
import com.bumptech.glide.Priority
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.Options
import com.bumptech.glide.load.data.DataFetcher
import com.bumptech.glide.load.engine.Resource
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.load.model.ModelLoader
import com.bumptech.glide.load.model.ModelLoaderFactory
import com.bumptech.glide.load.model.MultiModelLoaderFactory
import com.bumptech.glide.load.resource.SimpleResource
import com.bumptech.glide.load.resource.transcode.ResourceTranscoder
import kotlin.math.min


internal class BlurHashFetcher(val hash: BlurHashSource, val width: Int, val height: Int) :
    DataFetcher<BlurHash> {
    override fun cancel() = Unit

    override fun cleanup() = Unit

    override fun getDataClass(): Class<BlurHash> {
        return BlurHash::class.java
    }

    override fun getDataSource(): DataSource {
        return DataSource.LOCAL
    }

    override fun loadData(priority: Priority, callback: DataFetcher.DataCallback<in BlurHash>) {
        return callback.onDataReady(BlurHash(hash.hash, width = width, height = height))
    }

}

internal class BlurHashModelLoader : ModelLoader<BlurHashSource, BlurHash> {
    override fun buildLoadData(
        model: BlurHashSource,
        width: Int,
        height: Int,
        options: Options
    ): ModelLoader.LoadData<BlurHash> {
        return ModelLoader.LoadData(GlideUrl(model.hash), BlurHashFetcher(model, width = width, height = height))
    }


    override fun handles(model: BlurHashSource): Boolean {
        return true
    }
    class Factory : ModelLoaderFactory<BlurHashSource, BlurHash> {
        override fun build(multiFactory: MultiModelLoaderFactory): ModelLoader<BlurHashSource, BlurHash> {
            return BlurHashModelLoader()
        }

        override fun teardown() = Unit
    }
}

internal class BlurHashTransCoder(val context: Context) : ResourceTranscoder<BlurHash, BitmapDrawable> {
    override fun transcode(
        toTranscode: Resource<BlurHash>,
        options: Options
    ): Resource<BitmapDrawable> {
        val blurHash = toTranscode.get()
        val (width, height) = scaleToMax(blurHash.width, blurHash.height)
        val bitmap = BlurHashDecoder.decode(toTranscode.get().hash, width = width, height = height)
        return SimpleResource(BitmapDrawable(context.resources, bitmap))
    }
    private fun scaleToMax(width: Int, height: Int): Pair<Int, Int> {
        val smallWidth = min(480, min(width, height))
        val scale = smallWidth.toDouble() / width
        val smallHeight = scale * height
        return smallWidth to smallHeight.toInt()
    }
}
