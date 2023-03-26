package dao

import (
	"gorm.io/gorm"
	"systems.panta.milktea/push-to-fcm/pkg/repository"
)

type RepositoryModuleImpl struct {
	DB *gorm.DB
}

func (r *RepositoryModuleImpl) GetClientAccountRepository() repository.ClientAccountRepository {
	return &ClientAccountRepositoryImpl{
		DB: r.DB,
	}
}

func (r *RepositoryModuleImpl) GetPushSubscriptionRepository() repository.PushNotificationRepository {
	return &PushSubscriptionRepositoryImpl{
		DB: r.DB,
	}
}
