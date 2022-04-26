package jp.panta.misskeyandroidclient.di.module

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import jp.panta.misskeyandroidclient.impl.CheckEmojiAndroidImpl
import net.pantasystem.milktea.model.notes.reaction.CheckEmoji
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object CheckEmojiModule {

    @Singleton
    @Provides
    fun provideCheckEmoji(): CheckEmoji {
        return CheckEmojiAndroidImpl()
    }
}