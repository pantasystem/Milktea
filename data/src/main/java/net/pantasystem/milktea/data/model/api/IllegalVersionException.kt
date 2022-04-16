package net.pantasystem.milktea.data.model.api

import java.lang.IllegalStateException

class IllegalVersionException : IllegalStateException("要求されたバージョンに満たないAPIです。")