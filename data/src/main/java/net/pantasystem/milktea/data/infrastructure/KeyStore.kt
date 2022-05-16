package net.pantasystem.milktea.data.infrastructure

object KeyStore {

    enum class BooleanKey(val default: Boolean){
        INCLUDE_MY_RENOTES(true),
        INCLUDE_RENOTED_MY_NOTES(true),
        INCLUDE_LOCAL_RENOTES(true),


        IS_LEARN_NOTE_VISIBILITY(true),
    }
}