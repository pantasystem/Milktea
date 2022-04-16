package jp.panta.misskeyandroidclient.model.api

import net.pantasystem.milktea.data.api.misskey.MisskeyAPIServiceBuilder
import net.pantasystem.milktea.data.api.misskey.v11.MisskeyAPIV11
import net.pantasystem.milktea.data.model.api.Version
import org.junit.Assert
import org.junit.Test

class APITest {

    @Test
    fun testV11Following(){
        val api = MisskeyAPIServiceBuilder.build("https://misskey.io", Version("12"))
        val v12 = api as? MisskeyAPIV11
        Assert.assertNotEquals(v12, null)

    }
}