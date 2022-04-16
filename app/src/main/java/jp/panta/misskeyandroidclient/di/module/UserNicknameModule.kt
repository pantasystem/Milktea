package jp.panta.misskeyandroidclient.di.module

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import net.pantasystem.milktea.data.model.users.impl.UserNicknameDAO
import net.pantasystem.milktea.data.model.users.impl.UserNicknameRepositoryOnMemoryImpl
import net.pantasystem.milktea.data.model.users.impl.UserNicknameRepositorySQLiteImpl
import net.pantasystem.milktea.data.model.users.nickname.UserNicknameRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object UserNicknameModule {

    @Provides
    @Singleton
    fun provideUserNicknameRepository(userNicknameDAO: UserNicknameDAO): UserNicknameRepository {
        return UserNicknameRepositorySQLiteImpl(
            userNicknameDAO,
            UserNicknameRepositoryOnMemoryImpl()
        )
    }
}