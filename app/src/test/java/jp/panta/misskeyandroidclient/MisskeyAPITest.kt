package jp.panta.misskeyandroidclient

import jp.panta.misskeyandroidclient.model.MisskeyAPIServiceBuilder
import jp.panta.misskeyandroidclient.model.meta.RequestMeta
import jp.panta.misskeyandroidclient.model.notes.NoteRequest
import org.junit.Assert
import org.junit.Test
import org.junit.runners.Suite

@Suite.SuiteClasses(SecretConstantTest::class)
class MisskeyAPITest {

    val misskeyAPI = MisskeyAPIServiceBuilder.build("https://misskey.io")
    @Test
    fun testFavorites(){
        val res = misskeyAPI.favorites(NoteRequest(i = SecretConstantTest.i()))
        val list = res.execute().body()
        println(list)
        Assert.assertNotEquals(list, null)
        assert(! list.isNullOrEmpty())
    }

    @Test
    fun testGetMeta(){
        val res = misskeyAPI.getMeta(RequestMeta()).execute()
        val meta = res.body()
        println(meta)
        Assert.assertNotEquals(meta, null)

    }

}