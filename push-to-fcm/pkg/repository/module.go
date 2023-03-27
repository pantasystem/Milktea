package repository

type Module interface {
	GetClientAccountRepository() ClientAccountRepository
	GetPushNotificationRepository() PushNotificationRepository
}
