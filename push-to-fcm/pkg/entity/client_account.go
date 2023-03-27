package entity

import (
	"time"

	"github.com/google/uuid"
	"gorm.io/gorm"
)

type ClientAccount struct {
	ID          uuid.UUID `json:"id" gorm:"type:uuid;primary_key;default:uuid_generate_v4()"`
	Token       string    `json:"-" gorm:"type:varchar(255);unique_index"`
	DeviceToken *string   `json:"-" gorm:"type:varchar(255);unique_index"`
	Lang        *string   `json:"lang" gorm:"type:varchar(255);index"`
	CreatedAt   time.Time `json:"created_at"`
	UpdatedAt   time.Time `json:"updated_at"`
}

func (r *ClientAccount) BeforeCreate(tx *gorm.DB) error {
	uuid, err := uuid.NewRandom()
	if err != nil {
		return err
	}
	r.ID = uuid

	return nil
}
