package jp.panta.misskeyandroidclient.di.module

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import jp.panta.misskeyandroidclient.ui.PageableFragmentFactoryImpl
import net.pantasystem.milktea.common_android_ui.PageableFragmentFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class PageableModule {

    @Binds
    @Singleton
    abstract fun bindsPageableFragmentFactory(impl: PageableFragmentFactoryImpl): PageableFragmentFactory

}