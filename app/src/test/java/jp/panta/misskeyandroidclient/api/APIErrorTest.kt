package jp.panta.misskeyandroidclient.api

import kotlinx.coroutines.runBlocking
import net.pantasystem.milktea.api.misskey.DefaultOkHttpClientProvider
import net.pantasystem.milktea.api.misskey.I
import net.pantasystem.milktea.api.misskey.MisskeyAPIServiceBuilder
import net.pantasystem.milktea.api.misskey.notes.CreateNote
import net.pantasystem.milktea.common.APIError
import net.pantasystem.milktea.common.throwIfHasError
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class APIErrorTest {

    private val misskeyAPI = MisskeyAPIServiceBuilder(DefaultOkHttpClientProvider()).build("https://misskey.io")


    @Test
    fun testClientError() {
        Assertions.assertThrows(APIError.ForbiddenException::class.java) {
            runBlocking {
                misskeyAPI.create(CreateNote("", text = null)).throwIfHasError()
            }
        }
    }


    //@Test(expected = APIError.AuthenticationException::class)
    @Test
    fun testAuthenticationError() {
        Assertions.assertThrows(APIError.AuthenticationException::class.java) {
            runBlocking {
                val res = misskeyAPI.i(I(null))
                res.throwIfHasError()
            }

        }
        Assertions.assertTrue(true)
    }


    @Test
    fun testHasErrorBody(): Unit = runBlocking {
        val res = misskeyAPI.i(I(null))

        try {
            res.throwIfHasError()
        } catch (e: APIError) {
            Assertions.assertNotNull(e.error)
        }
    }

}