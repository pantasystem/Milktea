package repository

import (
	"context"

	"github.com/google/uuid"
	"systems.panta.milktea/push-to-fcm/pkg/entity"
)

type PushNotificationRepository interface {
	FindBy(ctx context.Context, query FindByQuery) (*entity.PushSubscription, error)
	Create(ctx context.Context, subscription *entity.PushSubscription) (*entity.PushSubscription, error)
	Delete(ctx context.Context, id uuid.UUID) error
	FindOne(ctx context.Context, id uuid.UUID) (*entity.PushSubscription, error)
}

type FindByQuery struct {
	Acct            string
	InstanceUri     string
	ClientAccountId uuid.UUID
}
