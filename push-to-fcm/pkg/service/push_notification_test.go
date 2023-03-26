package service_test

import (
	"encoding/base64"
	"fmt"
	"testing"

	"systems.panta.milktea/push-to-fcm/pkg/service"
)

func TestPushNotificationService_GenerateKey(t *testing.T) {
	s := &service.PushNotificationService{}

	keys, err := s.GenerateKey()
	if err != nil {
		t.Fatalf("Unexpected error: %v", err)
	}

	if keys.Auth == "" || keys.Public == "" || keys.Private == "" {
		t.Fatalf("Unexpected empty field(s) in generated key")
	}

	_, err = base64.URLEncoding.DecodeString(keys.Auth)
	if err != nil {
		t.Fatalf("Error decoding auth base64: %v", err)
	}

	_, err = base64.URLEncoding.DecodeString(keys.Public)
	if err != nil {
		t.Fatalf("Error decoding public key base64: %v", err)
	}

	_, err = base64.URLEncoding.DecodeString(keys.Private)
	if err != nil {
		t.Fatalf("Error decoding private key base64: %v", err)
	}
	fmt.Printf("Keys: %+v", keys)

}
