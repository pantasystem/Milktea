package service

import (
	"context"
	"crypto/ecdsa"
	"crypto/elliptic"
	"crypto/rand"
	"encoding/base64"
	"errors"

	"github.com/google/uuid"
	"gorm.io/gorm"
	"systems.panta.milktea/push-to-fcm/pkg/api/mastodon"
	"systems.panta.milktea/push-to-fcm/pkg/api/misskey"
	"systems.panta.milktea/push-to-fcm/pkg/config"
	"systems.panta.milktea/push-to-fcm/pkg/entity"
	"systems.panta.milktea/push-to-fcm/pkg/repository"
)

type PushNotificationService struct {
	PushNotificationRepository repository.PushNotificationRepository
	ClientAccountRepository    repository.ClientAccountRepository
	Config                     *config.Config
}

func (s *PushNotificationService) Subscribe(ctx context.Context, clientAccountID uuid.UUID, args SubscribeArgs) (*entity.PushSubscription, error) {
	ca, err := s.ClientAccountRepository.FindById(ctx, clientAccountID)
	if err != nil {
		return nil, err
	}
	sub, err := s.PushNotificationRepository.FindBy(ctx, repository.FindByQuery{
		Acct:            args.Acct,
		InstanceUri:     args.InstanceUri,
		ClientAccountId: ca.ID,
	})
	if err != nil {
		if !errors.Is(err, gorm.ErrRecordNotFound) {
			return nil, err
		}
	}
	if sub != nil {
		return sub, nil
	}

	keys, err := s.GenerateKey()
	if err != nil {
		return nil, err
	}

	sub, err = s.PushNotificationRepository.Create(ctx, &entity.PushSubscription{
		Acct:            args.Acct,
		InstanceUri:     args.InstanceUri,
		ClientAccountId: ca.ID,
		PublicKey:       keys.Public,
		PrivateKey:      keys.Private,
		Auth:            keys.Auth,
		ProviderType:    args.ProviderType,
	})

	if err != nil {
		return nil, err
	}

	if sub == nil {
		return nil, errors.New("failed to create subscription")
	}

	endpoint := s.Config.ServerUrl + "/api/subscriptions/" + sub.ID.String() + "/callbacks"
	switch args.ProviderType {
	case entity.ProviderTypeMastodon:
		c := mastodon.NotificationSubscriptionClient{
			BaseUrl: args.InstanceUri,
			Token:   ca.Token,
		}
		_, err := c.Subscribe(ctx, mastodon.PushSubscriptionRequest{
			Subscrption: mastodon.Subscrption{
				Endpoint: endpoint,
				Keys: mastodon.Keys{
					Auth:   sub.Auth,
					P256dh: sub.PublicKey,
				},
			},
			Data: mastodon.Data{
				Policy: "all",
			},
		})
		if err != nil {
			s.rollback(ctx, sub)
			return nil, err
		}

	case entity.ProviderTypeMisskey:
		c := misskey.SWSubscription{
			BaseUrl: args.InstanceUri,
			Token:   args.Token,
		}
		_, err := c.Subscribe(ctx, misskey.SubscribeRequest{
			Endpoint:  endpoint,
			PublicKey: sub.PublicKey,
			Auth:      sub.Auth,
		})
		if err != nil {
			s.rollback(ctx, sub)
			return nil, err
		}
	}
	// TODO
	return sub, nil
}

func (s *PushNotificationService) GenerateKey() (*Keys, error) {
	privateKey, err := ecdsa.GenerateKey(elliptic.P256(), rand.Reader)
	if err != nil {
		return nil, err
	}

	publicKeyBytes := elliptic.Marshal(privateKey.PublicKey.Curve, privateKey.PublicKey.X, privateKey.PublicKey.Y)
	publicKeyBase64 := base64.URLEncoding.EncodeToString(publicKeyBytes)

	privateKeyBytes := privateKey.D.Bytes()
	privateKeyBase64 := base64.URLEncoding.EncodeToString(privateKeyBytes)

	authBytes := make([]byte, 16)
	if _, err := rand.Read(authBytes); err != nil {
		return nil, err
	}
	authBase64 := base64.URLEncoding.EncodeToString(authBytes)

	return &Keys{
		Auth:    authBase64,
		Public:  publicKeyBase64,
		Private: privateKeyBase64,
	}, nil

}

func (s *PushNotificationService) rollback(ctx context.Context, sub *entity.PushSubscription) error {
	return s.PushNotificationRepository.Delete(ctx, sub.ID)
}

type Keys struct {
	Auth    string
	Public  string
	Private string
}

type SubscribeArgs struct {
	InstanceUri  string              `json:"instance_uri"`
	Token        string              `json:"token"`
	ProviderType entity.ProviderType `json:"provider_type"`
	Acct         string              `json:"acct"`
}
