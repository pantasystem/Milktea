package jp.panta.misskeyandroidclient

import jp.panta.misskeyandroidclient.model.I
import jp.panta.misskeyandroidclient.model.MisskeyAPIServiceBuilder
import jp.panta.misskeyandroidclient.model.notes.NoteRequest
import org.junit.Assert
import org.junit.Test

import org.junit.Assert.*
import org.junit.Before
import org.junit.runner.RunWith
import org.junit.runners.Suite

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@Suite.SuiteClasses(SecretConstantTest::class)
class ExampleUnitTest {

    val i = SecretConstantTest.i()

    @Test
    fun addition_isCorrect() {
        //assertEquals(4, 2 + 2)
        val api  =MisskeyAPIServiceBuilder.build("https://misskey.io")
        val res = api.searchNote(NoteRequest(SecretConstantTest.i(), query = "おはよう")).execute()
        println("${res.body()}, ${res.code()}, ${res.message()}")
        Assert.assertNotEquals(res.body(), null)
    }
}
