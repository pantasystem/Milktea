package service

import (
	"context"
	"crypto/ecdsa"
	"crypto/elliptic"
	"crypto/rand"
	"encoding/base64"

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
