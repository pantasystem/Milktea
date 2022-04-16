package net.pantasystem.milktea.data.model.messaging

class MessageNotFoundException(messageId: Message.Id) : NoSuchElementException("$messageId は見つかりませんでした")