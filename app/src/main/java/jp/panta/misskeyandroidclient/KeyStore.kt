package jp.panta.misskeyandroidclient

object KeyStore {

    enum class IntKey{

    }

    enum class StringKey{

    }

    enum class BooleanKey(val default: Boolean){
        INCLUDE_MY_RENOTES(true),
        INCLUDE_RENOTED_MY_NOTES(true),
        INCLUDE_LOCAL_RENOTES(true),
        AUTO_LOAD_TIMELINE(true),

        CAPTURE_NOTE_WHEN_STOPPED(true),
        AUTO_LOAD_TIMELINE_WHEN_STOPPED(true),

        HIDE_REMOVED_NOTE(true)

    }
}