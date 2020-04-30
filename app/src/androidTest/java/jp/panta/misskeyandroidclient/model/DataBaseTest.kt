package jp.panta.misskeyandroidclient.model

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.runner.AndroidJUnit4
import jp.panta.misskeyandroidclient.model.auth.KeyStoreSystemEncryption
import jp.panta.misskeyandroidclient.model.core.*
import jp.panta.misskeyandroidclient.viewmodel.setting.page.PageableTemplate
import org.hamcrest.Matchers.*
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@Suppress("DEPRECATION")
@RunWith(AndroidJUnit4::class)
class DataBaseTest{

    lateinit var db: DataBase
    lateinit var accountDao: AccountDao
    lateinit var connectionInformationDao: ConnectionInformationDao
    lateinit var pageDao: PageDao

    lateinit var encryption: Encryption

    @Before
    fun init(){
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, DataBase::class.java).build()
        accountDao = db.accountDao()
        connectionInformationDao = db.connectionInformationDao()
        encryption = KeyStoreSystemEncryption(context)
        pageDao = db.pageDao()
    }

    @Test
    @Throws(Exception::class)
    fun writeReadTest(){
        // make account
        val account = Account("114514")
        accountDao.insert(account)
        val ci = EncryptedConnectionInformation(
            account.id,
            "https://misskey.io",
            encryption.encrypt(account.id, "1145141919810"),
            null
        )
        connectionInformationDao.add(ci)

        val ci2 = ci.copy(encryptedI = encryption.encrypt(account.id, "hansin334"))
        connectionInformationDao.add(ci2)

        val read = accountDao.findAllSetting()
        assert(read[0].connectionInformationList.isNotEmpty())
        Assert.assertThat(read[0].connectionInformationList, not(`is`(empty<EncryptedConnectionInformation>())))
        pageDao.insert(
            Page("114514", "Global", 1, globalTimeline = Page.GlobalTimeline()).apply{

            }
        )
        val pages = pageDao.findAll()
        Assert.assertNotEquals(pages?.firstOrNull(), null)
        Assert.assertEquals(pages?.firstOrNull()?.accountId, "114514")

        Assert.assertNotEquals(pages?.firstOrNull()?.globalTimeline ,null)

        val newAccount = accountDao.findSettingByAccountId("114514")

        Assert.assertNotEquals(newAccount?.pages?.first()?.globalTimeline, null)

    }


}