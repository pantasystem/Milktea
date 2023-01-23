package jp.panta.misskeyandroidclient.di.module

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import jp.panta.misskeyandroidclient.impl.PageDefaultStringsOnAndroid
import net.pantasystem.milktea.model.account.MakeDefaultPagesUseCase
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AccountModule {


    @Provides
    @Singleton
    fun provideMakeDefaultPagesUseCase(
        @ApplicationContext context: Context
    ) : MakeDefaultPagesUseCase {
        return MakeDefaultPagesUseCase(
            PageDefaultStringsOnAndroid(context)
        )
    }
}