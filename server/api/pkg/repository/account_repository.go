package repository

import (
	"github.com/google/uuid"
	"systems.panta.milktea/pkg/domain"
)

type AdAccountRepository interface {
	FindByEmail(email string) (*domain.AdAccount, error)
	Create(user *domain.AdAccount) (*domain.AdAccount, error)
	Delete(id uuid.UUID) error
	FindOne(id uuid.UUID) (*domain.AdAccount, error)
	FindByToken(token string) (*domain.AdAccount, error)
	CreateToken(userId uuid.UUID, ipAddress string) (*domain.Token, error)
	RemoveToken(token domain.Token) error
}
