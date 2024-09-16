package systems.panta.benchmark

import android.content.Context
import androidx.benchmark.macro.StartupMode
import androidx.benchmark.macro.StartupTimingMetric
import androidx.benchmark.macro.junit4.MacrobenchmarkRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import net.pantasystem.milktea.common.getPreferenceName
import net.pantasystem.milktea.data.di.module.DbModule
import net.pantasystem.milktea.data.infrastructure.account.db.MediatorAccountRepository
import net.pantasystem.milktea.data.infrastructure.account.db.RoomAccountRepository
import net.pantasystem.milktea.data.infrastructure.auth.KeyStoreSystemEncryption
import net.pantasystem.milktea.model.account.Account
import net.pantasystem.milktea.model.account.AccountRepository
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * This is an example startup benchmark.
 *
 * It navigates to the device's home screen, and launches the default activity.
 *
 * Before running this benchmark:
 * 1) switch your app's active build variant in the Studio (affects Studio runs only)
 * 2) add `<profileable android:shell="true" />` to your app's manifest, within the `<application>` tag
 *
 * Run this benchmark from Studio to see startup measurements, and captured system traces
 * for investigating your app's performance.
 */
@RunWith(AndroidJUnit4::class)
class ExampleStartupBenchmark {
    @get:Rule
    val benchmarkRule = MacrobenchmarkRule()

    lateinit var accountRepository: AccountRepository

    @Before
    fun setup() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext

        val database = DbModule.database(context)

        val preferences =
            InstrumentationRegistry.getInstrumentation().targetContext.getSharedPreferences(
                context.getPreferenceName(),
                Context.MODE_PRIVATE,
            )
        val encryption = KeyStoreSystemEncryption(context)

        val roomAccountRepository = RoomAccountRepository(
            database,
            preferences,
            database.accountDAO(),
            database.pageDAO(),
            encryption
        )

        accountRepository = MediatorAccountRepository(roomAccountRepository, Dispatchers.IO)

    }

    @Test
    fun startup() = benchmarkRule.measureRepeated(
        packageName = "jp.panta.misskeyandroidclient",
        metrics = listOf(StartupTimingMetric()),
        iterations = 5,
        startupMode = StartupMode.COLD,
        setupBlock = {
            val uiAutomation = InstrumentationRegistry.getInstrumentation().uiAutomation
            val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
            device.wait(Until.hasObject(By.pkg("jp.panta.misskeyandroidclient").depth(0)), 5000)
        }
    ) {
        pressHome()
        startActivityAndWait()
    }

    private fun setupAccount() {
        runBlocking {
            val added = accountRepository.add(
                Account(
                    remoteId = BuildConfig.ACCOUNT_REMOTE_ID,
                    instanceDomain = BuildConfig.INSTANCE_DOMMAIN,
                    userName = BuildConfig.USERNAME,
                    token = BuildConfig.TOKEN,
                    pages = emptyList(),
                    instanceType = Account.InstanceType.MISSKEY,
                )
            ).getOrThrow()
            println("added account:$added")
            println("accounts:${accountRepository.getCurrentAccount().getOrThrow()}")
        }
    }
}