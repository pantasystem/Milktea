package net.pantasystem.milktea.common.glide.blurhash

import com.bumptech.glide.load.Options
import com.bumptech.glide.load.ResourceDecoder
import com.bumptech.glide.load.engine.Resource
import com.bumptech.glide.load.resource.SimpleResource
import com.bumptech.glide.request.target.Target

internal class BlurHashResourceDecoder : ResourceDecoder<BlurHash, BlurHash> {
    override fun decode(
        source: BlurHash,
        width: Int,
        height: Int,
        options: Options
    ): Resource<BlurHash> {
        var bh = source
        if (width != Target.SIZE_ORIGINAL && height != bh.width) {
            bh = bh.copy(width = width)
        }
        if (height != Target.SIZE_ORIGINAL && height != bh.height) {
            bh = bh.copy(height = height)
        }
        return SimpleResource(bh)
    }

    override fun handles(source: BlurHash, options: Options): Boolean {
        return true
    }
}