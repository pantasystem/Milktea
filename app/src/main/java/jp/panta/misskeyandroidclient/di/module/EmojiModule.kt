package jp.panta.misskeyandroidclient.di.module

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import jp.panta.misskeyandroidclient.impl.CheckEmojiAndroidImpl
import net.pantasystem.milktea.model.note.reaction.CheckEmoji
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object EmojiModule {

    @Singleton
    @Provides
    fun provideCheckEmoji(
    ): CheckEmoji {
        return CheckEmojiAndroidImpl()
    }


}
