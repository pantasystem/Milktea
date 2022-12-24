package net.pantasystem.milktea.model.emoji

import net.pantasystem.milktea.model.account.Account

interface EmojiEventHandler {

    fun observe(account: Account?)
}