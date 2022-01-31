package jp.panta.misskeyandroidclient.api

import jp.panta.misskeyandroidclient.api.notes.CreateNote
import jp.panta.misskeyandroidclient.model.I
import junit.framework.TestCase
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

class APIErrorTest {

    private val  misskeyAPI = MisskeyAPIServiceBuilder.build("https://misskey.io")



//    @Test(expected = APIError.ForbiddenException::class)
//    fun testClientError(): Unit = runBlocking {
//        misskeyAPI.create(CreateNote("", text = null)).throwIfHasError()
//    }



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