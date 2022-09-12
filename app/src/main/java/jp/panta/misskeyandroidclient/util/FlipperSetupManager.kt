package jp.panta.misskeyandroidclient.util

import android.content.Context
import com.facebook.flipper.android.AndroidFlipperClient
import com.facebook.flipper.android.utils.FlipperUtils
import com.facebook.flipper.plugins.databases.DatabasesFlipperPlugin
import com.facebook.flipper.plugins.inspector.DescriptorMapping
import com.facebook.flipper.plugins.inspector.InspectorFlipperPlugin
import com.facebook.flipper.plugins.network.NetworkFlipperPlugin
import com.facebook.flipper.plugins.sharedpreferences.SharedPreferencesFlipperPlugin
import jp.panta.misskeyandroidclient.BuildConfig

object FlipperSetupManager {

    val networkFlipperPlugin: NetworkFlipperPlugin? by lazy {
        if (BuildConfig.DEBUG) {
            null
        } else {
            NetworkFlipperPlugin()
        }
    }

    fun setup(context: Context) {
        if (BuildConfig.DEBUG && FlipperUtils.shouldEnableFlipper(context)) {
            AndroidFlipperClient.getInstance(context).apply {
                addPlugin(InspectorFlipperPlugin(context, DescriptorMapping.withDefaults()))
                addPlugin(DatabasesFlipperPlugin(context))
                addPlugin(SharedPreferencesFlipperPlugin(context))
                addPlugin(networkFlipperPlugin)
            }.start()
        }
    }
}