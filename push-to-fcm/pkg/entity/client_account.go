package entity

import (
	"time"

	"github.com/google/uuid"
)

type ClientAccount struct {
	ID          uuid.UUID `json:"id" gorm:"type:uuid;primary_key;default:uuid_generate_v4()"`
	Token       string    `json:"token" gorm:"type:varchar(255);unique_index"`
	DeviceToken *string   `json:"device_token" gorm:"type:varchar(255);unique_index"`
	CreatedAt   time.Time `json:"created_at"`
	UpdatedAt   time.Time `json:"updated_at"`
}
