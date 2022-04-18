package jp.panta.misskeyandroidclient.api

import net.pantasystem.milktea.api.misskey.APIError
import net.pantasystem.milktea.api.misskey.MisskeyAPIServiceBuilder
import net.pantasystem.milktea.api.misskey.throwIfHasError
import net.pantasystem.milktea.api.misskey.notes.CreateNote
import kotlinx.coroutines.runBlocking
import net.pantasystem.milktea.api.misskey.I
import org.junit.Assert.*
import org.junit.Test

class APIErrorTest {

    private val  misskeyAPI = net.pantasystem.milktea.api.misskey.MisskeyAPIServiceBuilder.build("https://misskey.io")



    @Test(expected = net.pantasystem.milktea.api.misskey.APIError.ForbiddenException::class)
    fun testClientError(): Unit = runBlocking {
        misskeyAPI.create(CreateNote("", text = null)).throwIfHasError()
    }



    //@Test(expected = APIError.AuthenticationException::class)
    @Test
    fun testAuthenticationError() {
        assertThrows(net.pantasystem.milktea.api.misskey.APIError.AuthenticationException::class.java) {
            runBlocking {
                val res = misskeyAPI.i(net.pantasystem.milktea.api.misskey.I(null))
                res.throwIfHasError()
            }

        }
        assertTrue(true)
    }



    @Test
    fun testHasErrorBody(): Unit = runBlocking {
        val res = misskeyAPI.i(net.pantasystem.milktea.api.misskey.I(null))

        try{
            res.throwIfHasError()
        }catch(e: net.pantasystem.milktea.api.misskey.APIError) {
            assertNotNull(e.error)
        }
    }

}