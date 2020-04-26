package jp.panta.misskeyandroidclient

object KeyStore {

    enum class IntKey(val default: Int){
        THEME_WHITE(0),
        THEME_BLACK(1),
        THEME_DARK(2),
        THEME_BREAD(3),
        THEME(THEME_WHITE.default)
    }

    fun isNightTheme(key: IntKey): Boolean{
        return IntKey.THEME_BLACK == key
                || IntKey.THEME_DARK == key
    }
    enum class StringKey{

    }

    enum class BooleanKey(val default: Boolean){
        INCLUDE_MY_RENOTES(true),
        INCLUDE_RENOTED_MY_NOTES(true),
        INCLUDE_LOCAL_RENOTES(true),
        AUTO_LOAD_TIMELINE(true),

        CAPTURE_NOTE_WHEN_STOPPED(false),
        AUTO_LOAD_TIMELINE_WHEN_STOPPED(false),

        HIDE_REMOVED_NOTE(true),

        HIDE_BOTTOM_NAVIGATION(false),

        IS_USER_NAME_DEFAULT(true)
    }
}