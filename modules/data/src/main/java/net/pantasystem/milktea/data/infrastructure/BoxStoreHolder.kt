package net.pantasystem.milktea.data.infrastructure

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import io.objectbox.BoxStore
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BoxStoreHolder @Inject constructor(
    @ApplicationContext private val context: Context
) {

    val boxStore: BoxStore by lazy {
        MyObjectBox.builder().maxReaders(1024).androidContext(context).build()
    }
}