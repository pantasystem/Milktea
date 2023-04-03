package entity

import (
	"time"

	"github.com/google/uuid"
	"gorm.io/gorm"
)

type PushSubscription struct {
	ID              uuid.UUID     `json:"id" gorm:"type:uuid;primary_key;"`
	ProviderType    ProviderType  `json:"provider_type" gorm:"type:varchar(255);index"`
	Acct            string        `json:"acct" gorm:"type:varchar(255);index;unique_index:idx_acct_account_id_instance_uri;"`
	ClientAccountId uuid.UUID     `json:"client_account_id" gorm:"type:uuid;index;unique_index:idx_acct_account_id_instance_uri;"`
	ClientAccount   ClientAccount `json:"client_account" gorm:"foreignkey:ClientAccountId"`
	InstanceUri     string        `json:"instance_uri" gorm:"type:varchar(255);index;unique_index:idx_acct_account_id_instance_uri"`
	PublicKey       string        `json:"-" gorm:"type:varchar(255)"`
	PrivateKey      string        `json:"-"`
	Auth            string        `json:"-" gorm:"type:varchar(255)"`
	CreatedAt       time.Time     `json:"created_at"`
	UpdatedAt       time.Time     `json:"updated_at"`
}

type ProviderType string

const (
	ProviderTypeMisskey  ProviderType = "misskey"
	ProviderTypeMastodon ProviderType = "mastodon"
)

func (r *PushSubscription) BeforeCreate(tx *gorm.DB) error {
	uuid, err := uuid.NewRandom()
	if err != nil {
		return err
	}
	r.ID = uuid

	return nil
}
