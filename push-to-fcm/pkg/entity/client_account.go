package entity

import (
	"crypto/sha256"
	"encoding/hex"
	"time"

	"github.com/google/uuid"
	"gorm.io/gorm"
)

type ClientAccount struct {
	ID          uuid.UUID `json:"id" gorm:"type:uuid;primary_key;"`
	Token       string    `json:"-" gorm:"type:varchar(255);unique_index"`
	DeviceToken *string   `json:"-" gorm:"type:varchar(255);unique_index"`
	Lang        *string   `json:"lang" gorm:"type:varchar(255);index"`
	CreatedAt   time.Time `json:"created_at"`
	UpdatedAt   time.Time `json:"updated_at"`
}

func (r *ClientAccount) BeforeCreate(tx *gorm.DB) error {
	u, err := uuid.NewRandom()
	if err != nil {
		return err
	}
	r.ID = u

	t, err := uuid.NewRandom()
	if err != nil {
		return err
	}
	bt := getSHA256Binary(t.String())
	r.Token = hex.EncodeToString(bt)

	return nil
}

func getSHA256Binary(s string) []byte {
	r := sha256.Sum256([]byte(s))
	return r[:]
}
