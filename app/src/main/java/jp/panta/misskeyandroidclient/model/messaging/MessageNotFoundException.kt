package jp.panta.misskeyandroidclient.model.messaging

class MessageNotFoundException(messageId: Message.Id) : NoSuchElementException("$messageId は見つかりませんでした")