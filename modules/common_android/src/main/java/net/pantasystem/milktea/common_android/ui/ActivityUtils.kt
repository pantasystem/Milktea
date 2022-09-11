package net.pantasystem.milktea.common_android.ui

import android.content.Intent



const val EXTRA_PARENT = "jp.panta.misskeyandroidclient.EXTRA_PARENT"

enum class Activities{
    ACTIVITY_OUT_APP,
    ACTIVITY_IN_APP,
}

fun Intent.putActivity(activities: Activities){
    this.putExtra(EXTRA_PARENT, activities.ordinal)
}

fun Intent.getParentActivity(): Activities{
    return Activities.values()[getIntExtra(EXTRA_PARENT, Activities.ACTIVITY_OUT_APP.ordinal)]
}