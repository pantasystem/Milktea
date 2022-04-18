package net.pantasystem.milktea.model.instance

import java.lang.IllegalStateException

class IllegalVersionException : IllegalStateException("要求されたバージョンに満たないAPIです。")