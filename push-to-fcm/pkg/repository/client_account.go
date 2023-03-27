package repository

import (
	"context"

	"github.com/google/uuid"
	"systems.panta.milktea/push-to-fcm/pkg/entity"
)

type ClientAccountRepository interface {
	Create(ctx context.Context, entity *entity.ClientAccount) (*entity.ClientAccount, error)
	FindByToken(ctx context.Context, token string) (*entity.ClientAccount, error)
	Delete(ctx context.Context, id uuid.UUID) error
	FindById(ctx context.Context, id uuid.UUID) (*entity.ClientAccount, error)
}
