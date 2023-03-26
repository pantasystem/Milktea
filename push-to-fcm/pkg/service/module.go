package service

import "systems.panta.milktea/push-to-fcm/pkg/repository"

type Module struct {
	RepositoryModule repository.Module
}

func (r *Module) GetClientAccountService() ClientAccountService {
	return ClientAccountService{
		ClientAccountRepository: r.RepositoryModule.GetClientAccountRepository(),
	}
}

func (r *Module) GetPushNotificationService() PushNotificationService {
	return PushNotificationService{
		PushNotificationRepository: r.RepositoryModule.GetPushNotificationRepository(),
	}
}
