package domain

import (
	"time"

	"github.com/google/uuid"
)

type Account struct {
	Id                uuid.UUID
	CreatedAt         time.Time
	UpdatedAt         time.Time
	Email             string
	EncryptedPassword string
}
