package dao

import (
	"context"

	"github.com/google/uuid"
	"gorm.io/gorm"
	"systems.panta.milktea/push-to-fcm/pkg/entity"
	"systems.panta.milktea/push-to-fcm/pkg/repository"
)

type PushSubscriptionRepositoryImpl struct {
	DB *gorm.DB
}

func (r *PushSubscriptionRepositoryImpl) Create(ctx context.Context, entity *entity.PushSubscription) (*entity.PushSubscription, error) {
	err := r.DB.Create(entity).Error
	if err != nil {
		return nil, err
	}
	return entity, nil
}

func (r *PushSubscriptionRepositoryImpl) FindBy(ctx context.Context, query repository.FindByQuery) (*entity.PushSubscription, error) {
	var subscription entity.PushSubscription
	err := r.DB.Where("acct = ? AND instance_uri = ? AND client_account_id = ?", query.Acct, query.InstanceUri, query.ClientAccountId).First(&subscription).Error
	if err != nil {
		return nil, err
	}
	return &subscription, nil
}

func (r *PushSubscriptionRepositoryImpl) Delete(ctx context.Context, id uuid.UUID) error {
	err := r.DB.Delete(&entity.PushSubscription{}, id).Error
	if err != nil {
		return err
	}
	return nil
}
