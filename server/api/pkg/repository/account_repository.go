package repository

import (
	"github.com/google/uuid"
	"systems.panta.milktea/pkg/domain"
)

type AccountRepository interface {
	FindByEmail(email string) (*domain.Account, error)
	Create(user *domain.Account) (*domain.Account, error)
	Delete(id uuid.UUID) error
	FindOne(id uuid.UUID) (*domain.Account, error)
	FindByToken(token string) (*domain.Account, error)
	CreateToken(userId uuid.UUID, ipAddress string) (*domain.Token, error)
	RemoveToken(token domain.Token) error
}
