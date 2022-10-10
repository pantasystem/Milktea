package domain

import (
	"time"

	"github.com/google/uuid"
	"gorm.io/gorm"
)

type Token struct {
	ID        uuid.UUID `json:"id" gorm:"primaryKey"`
	Token     string    `json:"token" gorm:"unique"`
	AccountId uuid.UUID `json:"accountId" gorm:"foreignKey"`
	Account   Account   `json:"-"`
	CreatedAt time.Time `json:"createdAt"`
	UpdatedAt time.Time `json:"updatedAt"`
	IpAddress string    `json:"ipAddress"`
}

func (t *Token) BeforeCreate(tx *gorm.DB) (err error) {
	t.ID, err = uuid.NewRandom()
	if err != nil {
		return err
	}
	token, err := uuid.NewRandom()
	if err != nil {
		return err
	}
	t.Token = token.String()
	return err
}
