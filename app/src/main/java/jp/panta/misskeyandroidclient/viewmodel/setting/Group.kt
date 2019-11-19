package jp.panta.misskeyandroidclient.viewmodel.setting

import androidx.annotation.StringRes

class Group(
    @StringRes val title: Int?,
    val items: List<Shared>
) : Shared