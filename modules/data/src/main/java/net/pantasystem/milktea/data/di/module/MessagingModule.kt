package net.pantasystem.milktea.data.di.module

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.components.SingletonComponent
import net.pantasystem.milktea.app_store.messaging.MessagePagingStore
import net.pantasystem.milktea.data.infrastructure.messaging.*
import net.pantasystem.milktea.model.account.AccountRepository
import net.pantasystem.milktea.model.messaging.MessageObserver
import net.pantasystem.milktea.model.messaging.MessageRepository
import net.pantasystem.milktea.model.messaging.MessagingRepository
import net.pantasystem.milktea.model.messaging.UnReadMessages
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object MessagingModule {

    @Provides
    @Singleton
    fun inMemoryMessageDataSource(accountRepository: AccountRepository): InMemoryMessageDataSource {
        return InMemoryMessageDataSource(accountRepository)
    }

    @Provides
    @Singleton
    fun unreadMessages(inMem: InMemoryMessageDataSource): UnReadMessages {
        return inMem
    }

    @Provides
    @Singleton
    fun messageDataSource(inMem: InMemoryMessageDataSource): MessageDataSource {
        return inMem
    }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class MessagingBindsModule {

    @Binds
    @Singleton
    abstract fun messageRepository(
        messageRepositoryImpl: MessageRepositoryImpl
    ) : MessageRepository

    @Binds
    @Singleton
    abstract fun provideMessageObserve(
        messageObserverImpl: MessageObserverImpl
    ): MessageObserver

    @Binds
    @Singleton
    abstract fun messagingRepository(
        impl: MessagingRepositoryImpl
    ) : MessagingRepository
}

@Module
@InstallIn(ViewModelComponent::class)
abstract class MessagingBindsViewModelModule {
    @Binds
    abstract fun provideMessagePagingStore(impl: MessagePagingStoreImpl): MessagePagingStore
}