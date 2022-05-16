package net.pantasystem.milktea.data.infrastructure

object KeyStore {


    enum class AutoNoteExpandedContentSize(val default: Int) {
        HEIGHT(300)
    }

    enum class BooleanKey(val default: Boolean){
        INCLUDE_MY_RENOTES(true),
        INCLUDE_RENOTED_MY_NOTES(true),
        INCLUDE_LOCAL_RENOTES(true),
        AUTO_LOAD_TIMELINE(true),

        UPDATE_TIMELINE_IN_BACKGROUND(false),

        HIDE_REMOVED_NOTE(true),

        HIDE_BOTTOM_NAVIGATION(false),

        IS_USER_NAME_DEFAULT(true),
        IS_POST_BUTTON_TO_BOTTOM(false),
        IS_SIMPLE_EDITOR_ENABLED(false),
        IS_LEARN_NOTE_VISIBILITY(true),
    }
}