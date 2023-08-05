package jp.panta.misskeyandroidclient.setup

import android.content.Context
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import net.pantasystem.milktea.app_store.account.AccountStore
import net.pantasystem.milktea.model.account.ClientIdRepository
import net.pantasystem.milktea.model.setting.LocalConfigRepository
import javax.inject.Inject

class AppStateController @Inject constructor(
    private val accountStore: AccountStore,
    private val configRepository: LocalConfigRepository,
    private val clientIdRepository: ClientIdRepository,
    @ApplicationContext private val applicationContext: Context
) {


    suspend fun initializeSettings() {
        coroutineScope {
            launch {
                initAccountStore()
            }
            launch {
                manageCrashlyticsCollectionState()
            }
            launch {
                manageAnalyticsCollectionState()
            }
            setFirebaseUserIds()
        }

    }

    private suspend fun initAccountStore() {
        accountStore.initialize()
    }

    private suspend fun manageCrashlyticsCollectionState() {
        configRepository.observe().map {
            it.isCrashlyticsCollectionEnabled
        }.distinctUntilChanged().collect {
            FirebaseCrashlytics.getInstance()
                .setCrashlyticsCollectionEnabled(it.isEnable)
        }
    }

    private suspend fun manageAnalyticsCollectionState() {
        configRepository.observe().map {
            it.isAnalyticsCollectionEnabled
        }.distinctUntilChanged().collect {
            FirebaseAnalytics.getInstance(applicationContext)
                .setAnalyticsCollectionEnabled(it.isEnabled)
        }
    }

    private fun setFirebaseUserIds() {
        val clientId = clientIdRepository.getOrCreate().clientId
        FirebaseAnalytics.getInstance(applicationContext).setUserId(clientId)
        FirebaseCrashlytics.getInstance().setUserId(clientId)
    }
}
