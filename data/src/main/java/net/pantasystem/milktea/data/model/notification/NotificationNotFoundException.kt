package net.pantasystem.milktea.data.model.notification

class NotificationNotFoundException(notificationId: Notification.Id) : NoSuchElementException("Notificationは存在しません: $notificationId")