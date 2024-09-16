package jp.panta.misskeyandroidclient.util

import android.content.Context
import javax.inject.Inject

class EmptyDebuggerSetupManagerImpl @Inject constructor(): DebuggerSetupManager {

    override fun setup(context: Context) {

    }

}