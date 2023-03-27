package service

import (
	"context"
	"crypto/ecdsa"
	"crypto/elliptic"
	"crypto/rand"
	"encoding/base64"

	"github.com/google/uuid"
	"systems.panta.milktea/push-to-fcm/pkg/api/mastodon"
	"systems.panta.milktea/push-to-fcm/pkg/api/misskey"
	wellknown "systems.panta.milktea/push-to-fcm/pkg/api/well_known"
	"systems.panta.milktea/push-to-fcm/pkg/entity"
	"systems.panta.milktea/push-to-fcm/pkg/repository"
)

type PushNotificationService struct {
	PushNotificationRepository repository.PushNotificationRepository
	ClientAccountRepository    repository.ClientAccountRepository
}

func (s *PushNotificationService) Subscribe(ctx context.Context, clientAccountID uuid.UUID, args SubscribeArgs) (*entity.PushSubscription, error) {
	ca, err := s.ClientAccountRepository.FindById(ctx, clientAccountID)
	if err != nil {
		return nil, err
	}
	wcli := &wellknown.WellKnown{
		BaseUrl: args.InstanceUri,
	}
	nodeInfo, err := wcli.FindNodeInfo()
	if err != nil {
		return nil, err
	}

	if nodeInfo.IsMisskey() {
		mk := &misskey.AccountClient{
			BaseUrl: args.InstanceUri,
			Token:   args.Token,
		}
		self, err := mk.FindSelf(ctx)
		if err != nil {
			return nil, err
		}
		sub, err := s.PushNotificationRepository.FindBy(ctx, repository.FindByQuery{
			Acct:            self.UserName,
			InstanceUri:     args.InstanceUri,
			ClientAccountId: ca.ID,
		})
		if err != nil {
			return nil, err
		}

		if sub != nil {
			return sub, nil
		}
		// subcli := &misskey.SWSubscription{
		// 	BaseUrl: args.InstanceUri,
		// 	Token:   args.Token,
		// }

		return nil, nil
	}

	if nodeInfo.IsMastodon() {
		ms := &mastodon.AccountClient{
			BaseUrl: args.InstanceUri,
			Token:   args.Token,
		}
		self, err := ms.VerifyCredentials(ctx)
		if err != nil {
			return nil, err
		}
		sub, err := s.PushNotificationRepository.FindBy(ctx, repository.FindByQuery{
			Acct:            self.Acct,
			InstanceUri:     args.InstanceUri,
			ClientAccountId: ca.ID,
		})
		if err != nil {
			return nil, err
		}

		if sub != nil {
			return sub, nil
		}

	}

	s.PushNotificationRepository.FindBy(ctx, repository.FindByQuery{})
	context.TODO()
	// TODO
	return nil, nil
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

type Keys struct {
	Auth    string
	Public  string
	Private string
}

type SubscribeArgs struct {
	InstanceUri string `json:"instance_uri"`
	Token       string `json:"token"`
}
