package jp.panta.misskeyandroidclient.startup

import android.content.Context
import androidx.startup.Initializer
import jp.panta.misskeyandroidclient.di.entorypoint.InitializerEntryPoint

class DebuggerSetupInitializer : Initializer<Unit> {

    override fun create(context: Context) {
        return InitializerEntryPoint.resolve(context).debuggerSetupManager().setup(context)
    }

    override fun dependencies(): MutableList<Class<out Initializer<*>>> {
        return mutableListOf()
    }
}