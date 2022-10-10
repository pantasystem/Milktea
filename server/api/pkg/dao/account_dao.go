package dao

import (
	"github.com/google/uuid"
	"gorm.io/gorm"
	"gorm.io/gorm/clause"
	"systems.panta.milktea/pkg/domain"
	"systems.panta.milktea/pkg/repository"
)

type AdAccountDAO struct {
	db gorm.DB
}

func (r AdAccountDAO) FindByEmail(email string) (*domain.AdAccount, error) {
	var account *domain.AdAccount
	if result := r.db.Where("email = ?", email).First(&account); result.Error != nil {
		if result.Error == gorm.ErrRecordNotFound {
			return nil, nil
		}
		return nil, result.Error
	}
	return account, nil
}

func (r AdAccountDAO) Create(account *domain.AdAccount) (*domain.AdAccount, error) {
	if result := r.db.Create(account); result.Error != nil {
		return nil, result.Error
	}
	return r.FindOne(account.Id)
}

func (r AdAccountDAO) Delete(id uuid.UUID) error {
	if result := r.db.Delete(&domain.AdAccount{}, id); result.Error != nil {
		return result.Error
	}
	return nil
}

func (r AdAccountDAO) FindOne(id uuid.UUID) (*domain.AdAccount, error) {
	var account domain.AdAccount
	if result := r.db.Preload(clause.Associations).First(&account, id); result.Error != nil {
		return nil, result.Error
	}
	return &account, nil
}

func (r AdAccountDAO) FindByToken(token string) (*domain.AdAccount, error) {
	var tokenModel domain.Token
	if result := r.db.Where("token = ?", token).First(&tokenModel); result.Error != nil {
		return nil, result.Error
	}
	return r.FindOne(tokenModel.AdAccountId)
}

func (r AdAccountDAO) CreateToken(accountId uuid.UUID, ipAddress string) (*domain.Token, error) {
	token := domain.Token{
		AdAccountId: accountId,
		IpAddress:   ipAddress,
	}
	if result := r.db.Create(&token); result.Error != nil {
		return nil, result.Error
	}
	return &token, nil
}

func (r AdAccountDAO) RemoveToken(token domain.Token) error {
	result := r.db.Delete(&domain.Token{}, token.ID)
	return result.Error
}

func NewAccountDAO(db gorm.DB) repository.AdAccountRepository {
	return AdAccountDAO{db: db}
}
