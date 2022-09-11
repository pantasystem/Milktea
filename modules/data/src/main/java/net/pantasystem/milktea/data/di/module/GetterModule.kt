package net.pantasystem.milktea.data.di.module

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import net.pantasystem.milktea.data.gettters.MessageAdder
import net.pantasystem.milktea.data.gettters.MessageRelationGetterImpl
import net.pantasystem.milktea.model.messaging.MessageRelationGetter
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
abstract class GetterModule {

    @Binds
    @Singleton
    abstract fun provideMessageGetter(impl: MessageRelationGetterImpl): MessageRelationGetter

    @Binds
    @Singleton
    abstract fun provideMessageAdder(impl: MessageRelationGetterImpl): MessageAdder

}