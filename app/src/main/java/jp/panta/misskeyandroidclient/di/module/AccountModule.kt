package jp.panta.misskeyandroidclient.di.module

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import jp.panta.misskeyandroidclient.impl.PageDefaultStringsOnAndroid
import net.pantasystem.milktea.data.model.DataBase
import net.pantasystem.milktea.model.account.AccountRepository
import net.pantasystem.milktea.model.account.MakeDefaultPagesUseCase
import net.pantasystem.milktea.data.model.account.db.MediatorAccountRepository
import net.pantasystem.milktea.data.model.account.db.RoomAccountRepository
import net.pantasystem.milktea.common.getPreferences
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
        return MakeDefaultPagesUseCase(
            PageDefaultStringsOnAndroid(context)
        )
    }
}