package jp.panta.misskeyandroidclient.model

import jp.panta.misskeyandroidclient.viewmodel.setting.page.PageableTemplate
import org.junit.Assert
import org.junit.Test

class PageTest {

    @Test
    fun hashCodeTest(){
        val page = PageableTemplate.homeTimeline("Home")
        page.accountId = "oijwi"
        val page2 = page.copy()
        page2.accountId = "fiejf"
        val page3 = page

        Assert.assertNotEquals(page3.hashCode(), page2.hashCode())
    }
}