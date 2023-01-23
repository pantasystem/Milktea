package net.pantasystem.milktea.common.glide.blurhash

import com.bumptech.glide.load.Options
import com.bumptech.glide.load.ResourceDecoder
import com.bumptech.glide.load.engine.Resource
import com.bumptech.glide.load.resource.SimpleResource

internal class BlurHashResourceDecoder : ResourceDecoder<BlurHash, BlurHash> {
    override fun decode(
        source: BlurHash,
        width: Int,
        height: Int,
        options: Options
    ): Resource<BlurHash> {
        return SimpleResource(source)
    }

    override fun handles(source: BlurHash, options: Options): Boolean {
        return true
    }
}