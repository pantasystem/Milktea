package domain

import (
	"time"

	"github.com/google/uuid"
	"golang.org/x/crypto/bcrypt"
	"gorm.io/gorm"
)

type Account struct {
	Id                uuid.UUID `json:"id" gorm:"primaryKey"`
	CreatedAt         time.Time `json:"createdAt"`
	UpdatedAt         time.Time `json:"updatedAt"`
	Email             string    `json:"-" gorm:"unique"`
	EncryptedPassword string    `json:"-"`
	Tokens            []*Token  `json:"-"`
}

func (r *Account) BeforeCreate(tx *gorm.DB) (err error) {
	id, err := uuid.NewRandom()
	if err != nil {
		return nil
	}
	r.Id = id

	return nil
}

func (r *Account) SetPassword(password string) bool {
	hash, err := bcrypt.GenerateFromPassword([]byte(password), bcrypt.DefaultCost)
	if err != nil {
		return false
	}
	r.EncryptedPassword = string(hash)
	return true
}

func (r *Account) CheckPassword(password string) error {
	return bcrypt.CompareHashAndPassword([]byte(r.EncryptedPassword), []byte(password))
}
