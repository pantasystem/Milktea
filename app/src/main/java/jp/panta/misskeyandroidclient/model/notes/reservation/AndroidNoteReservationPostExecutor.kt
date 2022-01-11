package jp.panta.misskeyandroidclient.model.notes.reservation

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import jp.panta.misskeyandroidclient.AlarmNotePostReceiver
import jp.panta.misskeyandroidclient.model.notes.draft.DraftNote

class AndroidNoteReservationPostExecutor (
    val context: Context
): NoteReservationPostExecutor{

    override fun register(draftNote: DraftNote) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlarmNotePostReceiver::class.java)
        intent.putExtra("DRAFT_NOTE_ID", draftNote.draftNoteId!!)
        intent.putExtra("ACCOUNT_ID", draftNote.accountId)

        val flag = when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
                PendingIntent.FLAG_MUTABLE
            }
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.M -> {
                PendingIntent.FLAG_UPDATE_CURRENT
            }
            else -> PendingIntent.FLAG_UPDATE_CURRENT
        }
        val pendingIntent = PendingIntent.getBroadcast(context, (draftNote.draftNoteId!! % 1000).toInt(), intent, flag)

        // NOTE: 参考にした https://qiita.com/upft_rkoshida/items/8149605f751137b4c21c
        when {
            Build.VERSION.SDK_INT < Build.VERSION_CODES.M -> {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    draftNote.reservationPostingAt!!.time,
                    pendingIntent
                )
            }
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.M -> {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    draftNote.reservationPostingAt!!.time,
                    pendingIntent
                )
            }
        }
    }
}

