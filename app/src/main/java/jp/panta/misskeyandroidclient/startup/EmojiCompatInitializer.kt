package jp.panta.misskeyandroidclient.startup

import android.content.Context
import androidx.emoji2.bundled.BundledEmojiCompatConfig
import androidx.emoji2.text.EmojiCompat
import androidx.startup.Initializer

class EmojiCompatInitializer : Initializer<EmojiCompat> {
    override fun create(context: Context): EmojiCompat {
        EmojiCompat.init(
            BundledEmojiCompatConfig(context)
                .setReplaceAll(true)
                .setMetadataLoadStrategy(EmojiCompat.LOAD_STRATEGY_MANUAL)
        )
        EmojiCompat.get().load()
        return EmojiCompat.get()
    }

    override fun dependencies(): MutableList<Class<out Initializer<*>>> {
        return mutableListOf()
    }
}