package jp.panta.misskeyandroidclient.api

import jp.panta.misskeyandroidclient.api.notes.CreateNote
import jp.panta.misskeyandroidclient.model.I
import jp.panta.misskeyandroidclient.model.api.MisskeyAPI
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import kotlin.jvm.Throws

class APIErrorTest {

    lateinit var misskeyAPI: MisskeyAPI
    val formatter = Json {

    }

    @Before
    fun setup(){
        misskeyAPI = MisskeyAPIServiceBuilder.build("https://misskey.io")
    }

    @Test(expected = APIError.ForbiddenException::class)
    fun testClientError() {
        misskeyAPI.create(CreateNote("", text = null)).execute()?.throwIfHasError()
    }



    @Test(expected = APIError.AuthenticationException::class)
    fun testAuthenticationError() {
        val res = misskeyAPI.i(I(null)).execute()

        res.throwIfHasError()
    }


    @Test
    fun testHasErrorBody() {
        val res = misskeyAPI.i(I(null)).execute()

        try{
            res.throwIfHasError()
        }catch(e: APIError) {
            assertNotNull(e.error)
        }
    }

}