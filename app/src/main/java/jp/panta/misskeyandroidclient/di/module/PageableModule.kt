package jp.panta.misskeyandroidclient.di.module

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import jp.panta.misskeyandroidclient.ui.PageableFragmentFactory
import jp.panta.misskeyandroidclient.ui.PageableFragmentFactoryImpl

@Module
@InstallIn(SingletonComponent::class)
abstract class PageableModule {

    @Binds
    abstract fun bindsPageableFragmentFactory(impl: PageableFragmentFactoryImpl): PageableFragmentFactory

}