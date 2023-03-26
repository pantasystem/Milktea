package entity

import (
	"time"

	"github.com/google/uuid"
)

type PushSubscription struct {
	Id              uuid.UUID     `json:"id" gorm:"type:uuid;primary_key;default:uuid_generate_v4()"`
	ProviderType    string        `json:"provider_type" gorm:"type:varchar(255);index"`
	Acct            string        `json:"acct" gorm:"type:varchar(255);index;"`
	ClientAccountId uuid.UUID     `json:"client_account_id" gorm:"type:uuid;index;"`
	ClientAccount   ClientAccount `json:"client_account" gorm:"foreignkey:ClientAccountId"`
	InstanceUri     string        `json:"instance_uri" gorm:"type:varchar(255);index"`
	CreatedAt       time.Time     `json:"created_at"`
	UpdatedAt       time.Time     `json:"updated_at"`
}
