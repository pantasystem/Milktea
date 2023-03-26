package service

import (
	"context"

	"systems.panta.milktea/push-to-fcm/pkg/entity"
	"systems.panta.milktea/push-to-fcm/pkg/repository"
)

type PushNotificationService struct {
	PushNotificationRepository repository.PushNotificationRepository
}

func (s *PushNotificationService) Subscribe(ctx context.Context, clientAccountID uint, subscription *entity.PushSubscription) (*entity.PushSubscription, error) {
	context.TODO()
	// TODO
	return nil, nil
}
