package service

import (
	"context"
	"errors"

	"gorm.io/gorm"
	"systems.panta.milktea/push-to-fcm/pkg/entity"
	"systems.panta.milktea/push-to-fcm/pkg/repository"
)

type ClientAccountService struct {
	ClientAccountRepository repository.ClientAccountRepository
}

func (r *ClientAccountService) GetOrRegisterClientAccount(ctx context.Context, token *string) (*entity.ClientAccount, error) {
	if token == nil {
		return r.ClientAccountRepository.Create(
			ctx,
			&entity.ClientAccount{},
		)
	}

	clientAccount, err := r.ClientAccountRepository.FindByToken(ctx, *token)
	if err != nil {
		if !errors.Is(err, gorm.ErrRecordNotFound) {
			return nil, err
		}
		return r.ClientAccountRepository.Create(
			ctx,
			&entity.ClientAccount{},
		)
	}

	return clientAccount, nil
}
