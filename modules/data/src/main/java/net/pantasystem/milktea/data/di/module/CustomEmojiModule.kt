package net.pantasystem.milktea.data.di.module

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import net.pantasystem.milktea.data.infrastructure.emoji.CustomEmojiApiAdapter
import net.pantasystem.milktea.data.infrastructure.emoji.CustomEmojiApiAdapterImpl
import net.pantasystem.milktea.data.infrastructure.emoji.CustomEmojiAspectRatioDataSourceImpl
import net.pantasystem.milktea.data.infrastructure.emoji.CustomEmojiRepositoryImpl
import net.pantasystem.milktea.model.emoji.CustomEmojiAspectRatioDataSource
import net.pantasystem.milktea.model.emoji.CustomEmojiRepository
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
abstract class CustomEmojiModule {

    @Binds
    internal abstract fun customEmojiApiAdapter(impl: CustomEmojiApiAdapterImpl): CustomEmojiApiAdapter

    @Singleton
    @Binds
    internal abstract fun bindCustomEmojiRepository(impl: CustomEmojiRepositoryImpl): CustomEmojiRepository

    @Singleton
    @Binds
    internal abstract fun bindAspectRatioDataSource(impl: CustomEmojiAspectRatioDataSourceImpl): CustomEmojiAspectRatioDataSource
}