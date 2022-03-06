package jp.panta.misskeyandroidclient.api

import jp.panta.misskeyandroidclient.api.misskey.APIError
import jp.panta.misskeyandroidclient.api.misskey.MisskeyAPIServiceBuilder
import jp.panta.misskeyandroidclient.api.misskey.throwIfHasError
import jp.panta.misskeyandroidclient.api.misskey.notes.CreateNote
import jp.panta.misskeyandroidclient.model.I
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

class APIErrorTest {

    private val  misskeyAPI = MisskeyAPIServiceBuilder.build("https://misskey.io")



    @Test(expected = APIError.ForbiddenException::class)
    fun testClientError(): Unit = runBlocking {
        misskeyAPI.create(CreateNote("", text = null)).throwIfHasError()
    }



    //@Test(expected = APIError.AuthenticationException::class)
    @Test
    fun testAuthenticationError() {
        assertThrows(APIError.AuthenticationException::class.java) {
            runBlocking {
                val res = misskeyAPI.i(I(null))
                res.throwIfHasError()
            }

        }
        assertTrue(true)
    }



    @Test
    fun testHasErrorBody(): Unit = runBlocking {
        val res = misskeyAPI.i(I(null))

        try{
            res.throwIfHasError()
        }catch(e: APIError) {
            assertNotNull(e.error)
        }
    }

}