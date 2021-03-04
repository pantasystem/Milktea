package jp.panta.misskeyandroidclient.model.notification

class NotificationNotFoundException(notificationId: Notification.Id) : NoSuchElementException("Notificationは存在しません: $notificationId")