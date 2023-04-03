package service

import (
	"systems.panta.milktea/push-to-fcm/pkg/config"
	"systems.panta.milktea/push-to-fcm/pkg/repository"
)

type Module struct {
	RepositoryModule repository.Module
	Config           *config.Config
}

func (r *Module) GetClientAccountService() *ClientAccountService {
	return &ClientAccountService{
		ClientAccountRepository: r.RepositoryModule.GetClientAccountRepository(),
	}
}

func (r *Module) GetPushNotificationService() *PushNotificationService {
	return &PushNotificationService{
		PushNotificationRepository: r.RepositoryModule.GetPushNotificationRepository(),
		ClientAccountRepository:    r.RepositoryModule.GetClientAccountRepository(),
		Config:                     r.Config,
	}
}
