package jp.panta.misskeyandroidclient

import android.app.Application
import android.os.Looper
import android.util.Log
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.google.firebase.crashlytics.FirebaseCrashlytics
import dagger.hilt.android.HiltAndroidApp
import jp.panta.misskeyandroidclient.setup.AppStateController
import jp.panta.misskeyandroidclient.worker.WorkerJobInitializer
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import net.pantasystem.milktea.app_store.account.AccountStore
import net.pantasystem.milktea.common.Logger
import net.pantasystem.milktea.common_android.platform.activeNetworkFlow
import net.pantasystem.milktea.data.infrastructure.MemoryCacheCleaner
import net.pantasystem.milktea.data.infrastructure.streaming.ChannelAPIMainEventDispatcherAdapter
import net.pantasystem.milktea.data.infrastructure.streaming.MediatorMainEventDispatcher
import net.pantasystem.milktea.data.streaming.SocketWithAccountProvider
import javax.inject.Inject

//基本的な情報はここを返して扱われる
@HiltAndroidApp
class MiApplication : Application(), Configuration.Provider {

    @Inject
    internal lateinit var mAccountStore: AccountStore

    @Inject
    internal lateinit var mSocketWithAccountProvider: SocketWithAccountProvider


    @Inject
    internal lateinit var mainEventDispatcherFactory: MediatorMainEventDispatcher.Factory

    @Inject
    internal lateinit var channelAPIMainEventDispatcherAdapter: ChannelAPIMainEventDispatcherAdapter

    @Inject
    internal lateinit var applicationScope: CoroutineScope

    @Inject
    internal lateinit var lf: Logger.Factory

    private val logger: Logger by lazy {
        lf.create("MiApplication")
    }


    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    @Inject
    internal lateinit var memoryCacheCleaner: MemoryCacheCleaner

    @Inject
    internal lateinit var initWorkerJobs: WorkerJobInitializer

    @Inject
    internal lateinit var appStateController: AppStateController

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun onCreate() {
        super.onCreate()

        val defaultUncaughtExceptionHandler = Thread.getDefaultUncaughtExceptionHandler()
        val mainThreadId = Looper.getMainLooper().thread.id
        Thread.setDefaultUncaughtExceptionHandler { t, e ->
            FirebaseCrashlytics.getInstance().recordException(e)
            Log.e("MiApplication", "Thread上で致命的なエラーが発生しました thread id:${t.id}, name:${t.name}", e)
            if (mainThreadId == t.id) {
                defaultUncaughtExceptionHandler?.uncaughtException(t, e)
            }
        }

        val mainEventDispatcher = mainEventDispatcherFactory.create()
        channelAPIMainEventDispatcherAdapter(mainEventDispatcher)

        applicationScope.launch {
            appStateController.initializeSettings()
        }

        activeNetworkFlow().distinctUntilChanged().onEach {
            logger.debug { "接続状態が変化:${if (it) "接続" else "未接続"}" }
            mSocketWithAccountProvider.all().forEach { socket ->
                if (it) {
                    socket.onNetworkActive()
                } else {
                    socket.onNetworkInActive()
                }
            }
        }.catch { e ->
            logger.error("致命的なエラー", e)
        }.launchIn(applicationScope + Dispatchers.IO)


        enqueueWorkManagers()

    }

    override fun getWorkManagerConfiguration(): Configuration {
        return Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
    }

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)

        when (level) {
            TRIM_MEMORY_RUNNING_CRITICAL, TRIM_MEMORY_RUNNING_MODERATE, TRIM_MEMORY_MODERATE, TRIM_MEMORY_RUNNING_LOW -> {
                applicationScope.launch {
                    memoryCacheCleaner.clean()
                }
            }
            TRIM_MEMORY_BACKGROUND -> Unit
            TRIM_MEMORY_UI_HIDDEN -> Unit
            TRIM_MEMORY_COMPLETE -> Unit


        }
    }

    private fun enqueueWorkManagers() {
        initWorkerJobs()
    }
}