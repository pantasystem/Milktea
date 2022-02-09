package jp.panta.misskeyandroidclient.model.api

import java.lang.IllegalStateException

class IllegalVersionException : IllegalStateException("要求されたバージョンに満たないAPIです。")