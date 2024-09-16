package jp.panta.misskeyandroidclient.ui.main

import android.content.Intent
import jp.panta.misskeyandroidclient.BuildConfig
import jp.panta.misskeyandroidclient.ui.main.viewmodel.MainViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import net.pantasystem.milktea.model.account.Account
import net.pantasystem.milktea.model.account.AccountRepository
import javax.inject.Inject

/**
 * 起動時にIntentを受け取り、その中に認証情報が含まれていれば、
 * DBに認証情報を追加するための処理。
 * 脆弱性につながる可能性があるため、デバッグモード時とベンチマーク時には動作しないように実装してある。
 */
internal class IntentToAddAccountHandler(
    private val coroutineScope: CoroutineScope,
    private val mainViewModel: MainViewModel,
    private val accountRepository: AccountRepository,
) {

    class Factory @Inject constructor(
        private val accountRepository: AccountRepository,
    ) {
        fun create(
            coroutineScope: CoroutineScope,
            mainViewModel: MainViewModel,
        ): IntentToAddAccountHandler {
            return IntentToAddAccountHandler(
                coroutineScope,
                mainViewModel,
                accountRepository,
            )
        }
    }

    @Suppress("KotlinConstantConditions")
    operator fun invoke(intent: Intent) {
        if (BuildConfig.BUILD_TYPE == "benchmark" || BuildConfig.BUILD_TYPE == "debug") {
            mainViewModel.setShouldWaitForAuthentication(true)
            coroutineScope.launch {
                try {
                    val username = intent.getStringExtra("username") ?: return@launch
                    val host = intent.getStringExtra("host") ?: return@launch
                    val token = intent.getStringExtra("token") ?: return@launch
                    val remoteId = intent.getStringExtra("remoteId") ?: return@launch

                    accountRepository.add(
                        Account(
                            remoteId = remoteId,
                            instanceDomain = host,
                            userName = username,
                            token = token,
                            instanceType = Account.InstanceType.MISSKEY,
                        )
                    ).getOrThrow()
                } finally {
                    mainViewModel.setShouldWaitForAuthentication(false)
                }
            }
        }
    }
}