package jp.panta.misskeyandroidclient.di.module.gallery

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import jp.panta.misskeyandroidclient.model.gallery.GalleryDataSource
import jp.panta.misskeyandroidclient.model.gallery.impl.InMemoryGalleryDataSource
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class GalleryModule {

    @Binds
    @Singleton
    abstract fun galleryDataSource(galleryDataSource: InMemoryGalleryDataSource): GalleryDataSource
}