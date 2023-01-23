package net.pantasystem.milktea.model.messaging

class MessageNotFoundException(messageId: Message.Id) : NoSuchElementException("$messageId は見つかりませんでした")