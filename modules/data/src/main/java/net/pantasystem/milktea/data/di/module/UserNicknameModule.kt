package net.pantasystem.milktea.data.di.module

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import net.pantasystem.milktea.data.infrastructure.user.UserNicknameRepositorySQLiteImpl
import net.pantasystem.milktea.model.user.nickname.UserNicknameRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class UserNicknameModule {

    @Binds
    @Singleton
    abstract fun bindUserNicknameRepository(userNicknameRepositorySQLiteImpl: UserNicknameRepositorySQLiteImpl): UserNicknameRepository

}