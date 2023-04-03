package dao

import (
	"context"
	"fmt"

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
		fmt.Printf("create subscription error: %v\n", err)
		return nil, err
	}
	return r.FindOne(ctx, entity.ID)
}

func (r *PushSubscriptionRepositoryImpl) FindBy(ctx context.Context, query repository.FindByQuery) (*entity.PushSubscription, error) {
	var subscription entity.PushSubscription
	err := r.DB.Where("acct = ? AND instance_uri = ? AND client_account_id = ?", query.Acct, query.InstanceUri, query.ClientAccountId).First(&subscription).Error
	if err != nil {
		if err == gorm.ErrRecordNotFound {
			return nil, nil
		}
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

func (r *PushSubscriptionRepositoryImpl) FindOne(ctx context.Context, id uuid.UUID) (*entity.PushSubscription, error) {
	var subscription entity.PushSubscription
	err := r.DB.First(&subscription, id).Error
	if err != nil {
		return nil, err
	}
	return &subscription, nil
}
