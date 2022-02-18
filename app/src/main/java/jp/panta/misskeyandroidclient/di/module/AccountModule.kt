package jp.panta.misskeyandroidclient.di.module

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import jp.panta.misskeyandroidclient.model.DataBase
import jp.panta.misskeyandroidclient.model.account.AccountRepository
import jp.panta.misskeyandroidclient.model.account.MakeDefaultPagesUseCase
import jp.panta.misskeyandroidclient.model.account.PageDefaultStringsOnAndroid
import jp.panta.misskeyandroidclient.model.account.db.MediatorAccountRepository
import jp.panta.misskeyandroidclient.model.account.db.RoomAccountRepository
import jp.panta.misskeyandroidclient.util.getPreferenceName
import jp.panta.misskeyandroidclient.util.getPreferences
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AccountModule {

    @Provides
    @Singleton
    fun accountRepository(
        @ApplicationContext context: Context,
        database: DataBase,
    ): AccountRepository {
        val preferences = context.getPreferences()
        val roomAccountRepository = RoomAccountRepository(database, preferences, database.accountDAO(), database.pageDAO())
        return MediatorAccountRepository(roomAccountRepository)
    }

    @Provides
    @Singleton
    fun provideMakeDefaultPagesUseCase(
        @ApplicationContext context: Context
    ) : MakeDefaultPagesUseCase {
        return MakeDefaultPagesUseCase(PageDefaultStringsOnAndroid(context))
    }
}