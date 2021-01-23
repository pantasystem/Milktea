package jp.panta.misskeyandroidclient.model.api

import jp.panta.misskeyandroidclient.api.MisskeyAPIServiceBuilder
import jp.panta.misskeyandroidclient.model.users.RequestUser
import jp.panta.misskeyandroidclient.api.v11.MisskeyAPIV11
import org.junit.Assert
import org.junit.Test

class APITest {

    @Test
    fun testV11Following(){
        val api = MisskeyAPIServiceBuilder.build("https://misskey.io", Version("12"))
        val v12 = api as? MisskeyAPIV11
        Assert.assertNotEquals(v12, null)

        val response = v12!!.followers(RequestUser(i = null, userName = "Panta", host = null, userId = null)).execute().body()
        Assert.assertNotEquals(response, null)
        println(response)
    }
}