package jp.panta.misskeyandroidclient.ui.settings.viewmodel.page

import jp.panta.misskeyandroidclient.model.account.page.Page

interface PageSettingAction {

    fun action(page: Page?)

}