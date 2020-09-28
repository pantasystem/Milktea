package jp.panta.misskeyandroidclient

import android.app.Activity
import android.content.Intent
import android.util.Log
import jp.panta.misskeyandroidclient.MiActivityType.*
import jp.panta.misskeyandroidclient.view.settings.activities.*

enum class MiActivityType{
    MAIN_ACTIVITY,
    URL_PREVIEW_SOURCE_ACTIVITY,
    DRAFT_NOTES_ACTIVITY,
    SORTED_USERS_ACTIVITY,
    ANTENNA_EDITOR_ACTIVITY,
    ANTENNA_LIST_ACTIVITY,
    PAGE_SETTING_ACTIVITY,
    APP_AUTH_CALLBACK_ACTIVITY,
    APP_AUTH_ACTIVITY,
    MESSAGING_LIST_ACTIVITY,
    NOTIFICATIONS_ACTIVITY,
    REACTION_SETTING_ACTIVITY,
    SEARCH_AND_SELECT_USER_ACTIVITY,
    USER_LIST_DETAIL_ACTIVITY,
    LIST_LIST_ACTIVITY,
    FAVORITE_ACTIVITY,
    FOLLOW_FOLLOWER_ACTIVITY,
    SEARCH_ACTIVITY,
    SEARCH_RESULT_ACTIVITY,
    MEDIA_ACTIVITY,
    SETTING_APPEARANCE_ACTIVITY,
    SETTING_MOVEMENT_ACTIVITY,
    SETTINGS_ACTIVITY,
    USER_DETAIL_ACTIVITY,
    NOTE_DETAIL_ACTIVITY,
    MESSAGE_ACTIVITY,
    DRIVE_ACTIVITY,
    NOTE_EDITOR_ACTIVITY,
}

const val NAVIGATION_UP_TO = "jp.panta.misskeyandroidclient.UP_TO"

fun Activity.navigateUpTo(){
    val activityType = intent.getSerializableExtra(NAVIGATION_UP_TO)
    if(activityType is MiActivityType){
        navigateUpTo(activityType)
    }else{
        Log.w("ActivityNavigation", "NAVIGATION_UP_TOが設定されていません")
        finish()
    }

}
fun Activity.navigateUpTo(activityType: MiActivityType){

    val intent = createIntent(activityType)
    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK
    navigateUpTo(intent)
}

fun Activity.createIntent(activityType: MiActivityType): Intent{
    return when(activityType){
        MAIN_ACTIVITY -> Intent(this, MainActivity::class.java)
        URL_PREVIEW_SOURCE_ACTIVITY -> Intent(this, UrlPreviewSourceSettingActivity::class.java)
        DRAFT_NOTES_ACTIVITY -> Intent(this, DraftNotesActivity::class.java)
        SORTED_USERS_ACTIVITY -> Intent(this, SortedUsersActivity::class.java)
        ANTENNA_EDITOR_ACTIVITY -> Intent(this, AntennaEditorActivity::class.java)
        ANTENNA_LIST_ACTIVITY -> Intent(this, AntennaListActivity::class.java)
        PAGE_SETTING_ACTIVITY -> Intent(this, PageSettingActivity::class.java)
        APP_AUTH_CALLBACK_ACTIVITY -> Intent(this, AppAuthCallbackActivity::class.java)
        APP_AUTH_ACTIVITY -> Intent(this, AppAuthActivity::class.java)
        MESSAGING_LIST_ACTIVITY -> Intent(this, MessagingListActivity::class.java)
        NOTIFICATIONS_ACTIVITY -> Intent(this, NotificationsActivity::class.java)
        REACTION_SETTING_ACTIVITY -> Intent(this, ReactionSettingActivity::class.java)
        SEARCH_AND_SELECT_USER_ACTIVITY -> Intent(this, SearchAndSelectUserActivity::class.java)
        USER_LIST_DETAIL_ACTIVITY -> Intent(this, UserListDetailActivity::class.java)
        LIST_LIST_ACTIVITY -> Intent(this, ListListActivity::class.java)
        FAVORITE_ACTIVITY -> Intent(this, FavoriteActivity::class.java)
        FOLLOW_FOLLOWER_ACTIVITY -> Intent(this, FollowFollowerActivity::class.java)
        SEARCH_ACTIVITY -> Intent(this, SearchActivity::class.java)
        SEARCH_RESULT_ACTIVITY -> Intent(this, SearchResultActivity::class.java)
        MEDIA_ACTIVITY -> Intent(this, MediaActivity::class.java)
        SETTING_APPEARANCE_ACTIVITY -> Intent(this, SettingAppearanceActivity::class.java)
        SETTING_MOVEMENT_ACTIVITY -> Intent(this, SettingMovementActivity::class.java)
        SETTINGS_ACTIVITY -> Intent(this, SettingsActivity::class.java)
        USER_DETAIL_ACTIVITY -> Intent(this, UserDetailActivity::class.java)
        NOTE_DETAIL_ACTIVITY -> Intent(this, NoteDetailActivity::class.java)
        MESSAGE_ACTIVITY -> Intent(this, MessageActivity::class.java)
        DRIVE_ACTIVITY -> Intent(this, DriveActivity::class.java)
        NOTE_EDITOR_ACTIVITY -> Intent(this, NoteEditorActivity::class.java)
        //else -> Intent(this, MainActivity::class.java)
    }
}
