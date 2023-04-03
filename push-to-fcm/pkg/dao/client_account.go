package dao

import (
	"context"

	"github.com/google/uuid"
	"gorm.io/gorm"
	"systems.panta.milktea/push-to-fcm/pkg/entity"
)

type ClientAccountRepositoryImpl struct {
	DB *gorm.DB
}

func (r *ClientAccountRepositoryImpl) Create(ctx context.Context, entity *entity.ClientAccount) (*entity.ClientAccount, error) {
	err := r.DB.Create(entity).Error
	if err != nil {
		return nil, err
	}
	return entity, nil
}

func (r *ClientAccountRepositoryImpl) FindByToken(ctx context.Context, clientID string) (*entity.ClientAccount, error) {
	var clientAccount entity.ClientAccount
	err := r.DB.Where("token = ?", clientID).First(&clientAccount).Error
	if err != nil {
		return nil, err
	}
	return &clientAccount, nil
}

func (r *ClientAccountRepositoryImpl) Delete(ctx context.Context, id uuid.UUID) error {
	err := r.DB.Delete(&entity.ClientAccount{}, id).Error
	if err != nil {
		return err
	}
	return nil
}

func (r *ClientAccountRepositoryImpl) FindById(ctx context.Context, id uuid.UUID) (*entity.ClientAccount, error) {
	var clientAccount entity.ClientAccount
	err := r.DB.Where("id = ?", id).First(&clientAccount).Error
	if err != nil {
		return nil, err
	}
	return &clientAccount, nil
}

func (r *ClientAccountRepositoryImpl) Update(ctx context.Context, e *entity.ClientAccount) (*entity.ClientAccount, error) {
	result := r.DB.Model(entity.ClientAccount{}).Where("id = ?", e.ID).Updates(map[string]interface{}{
		"device_token": e.DeviceToken,
		"lang":         e.Lang,
	})
	if result.Error != nil {
		return nil, result.Error
	}
	return e, nil
}
