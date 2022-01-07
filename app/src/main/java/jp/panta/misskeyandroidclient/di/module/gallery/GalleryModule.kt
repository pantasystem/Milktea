package jp.panta.misskeyandroidclient.di.module.gallery

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import jp.panta.misskeyandroidclient.model.gallery.GalleryDataSource
import jp.panta.misskeyandroidclient.model.gallery.impl.InMemoryGalleryDataSource

@Module
@InstallIn(SingletonComponent::class)
abstract class GalleryModule {

    @Binds
    abstract fun galleryDataSource(galleryDataSource: InMemoryGalleryDataSource): GalleryDataSource
}