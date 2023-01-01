package net.pantasystem.milktea.common

import kotlin.math.pow

fun Long.convertToHumanReadable(): String {
    val size = this
    return if (size >= 2.0.pow(50)) {
        "${size / 2.0.pow(50).toLong()}PB"
    } else if(size >= 2.0.pow(40)) {
        "${size / 2.0.pow(40).toLong()}TB"
    } else if(size >= 1073741824L) {
        "${size / 1073741824L}GB"
    } else if(size >= 1048576L) {
        "${size / 1048576L}MB"
    } else if (size >= 1024) {
        "${size / 1024.0.pow(1.0).toLong()}KB"
    } else {
        "${size}B"
    }
}