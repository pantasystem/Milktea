package jp.panta.misskeyandroidclient.ui.main

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.crashlytics.FirebaseCrashlytics
import jp.panta.misskeyandroidclient.R
import kotlinx.coroutines.launch
import net.pantasystem.milktea.common_navigation.UserDetailNavigation
import net.pantasystem.milktea.common_navigation.UserDetailNavigationArgs
import net.pantasystem.milktea.model.account.AccountRepository
import net.pantasystem.milktea.model.notes.Note
import net.pantasystem.milktea.model.notification.PushNotification
import net.pantasystem.milktea.model.notification.toPushNotification
import net.pantasystem.milktea.model.user.User
import net.pantasystem.milktea.note.NoteDetailActivity

/**
 * MainActivity起動時にIntentを処理するクラス
 * 例えばバックグラウンドからのFCMのプッシュ通知を開かれた時は
 * Intentに通知情報が入っているので、その情報を処理して通知に対応した画面に遷移するようにする。
 */
class MainActivityInitialIntentHandler(
    private val bottomNavigationView: BottomNavigationView,
    private val activity: AppCompatActivity,
    private val userDetailNavigation: UserDetailNavigation,
    private val accountRepository: AccountRepository,
) {

    operator fun invoke(intent: Intent) {
        // NOTE: バックグラウンドから通知を開いた場合、通知のデータがintentに入っている
        val pushNotification = intent.extras?.toPushNotification()?.getOrNull()
        if (pushNotification != null) {
            activity.lifecycleScope.launch {
                accountRepository.get(pushNotification.accountId).mapCatching {
                    accountRepository.setCurrentAccount(it).getOrThrow()
                }.onSuccess {
                    navigateBy(pushNotification)
                }.onFailure {
                    FirebaseCrashlytics.getInstance().recordException(it)
                }
            }
            return
        }
    }

    private fun navigateBy(pushNotification: PushNotification) {
        bottomNavigationView.selectedItemId = R.id.navigation_notification
        when {
            pushNotification.isNearUserNotification() -> {
                activity.startActivity(
                    userDetailNavigation.newIntent(
                        UserDetailNavigationArgs.UserId(
                            userId = User.Id(
                                pushNotification.accountId,
                                pushNotification.userId!!
                            )
                        ))
                )
            }
            pushNotification.isNearNoteNotification() -> {
                activity.startActivity(
                    NoteDetailActivity.newIntent(activity, Note.Id(
                        pushNotification.accountId,
                        pushNotification.noteId!!,
                    ))
                )
            }
        }
    }
}