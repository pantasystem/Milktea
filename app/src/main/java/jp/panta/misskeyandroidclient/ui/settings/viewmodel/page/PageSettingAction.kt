package jp.panta.misskeyandroidclient.ui.settings.viewmodel.page

import net.pantasystem.milktea.data.model.account.page.Page

interface PageSettingAction {

    fun action(page: Page?)

}