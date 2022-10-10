package dao

import (
	"github.com/google/uuid"
	"gorm.io/gorm"
	"gorm.io/gorm/clause"
	"systems.panta.milktea/pkg/domain"
	"systems.panta.milktea/pkg/repository"
)

type AccountDAO struct {
	db gorm.DB
}

func (r AccountDAO) FindByEmail(email string) (*domain.Account, error) {
	var account *domain.Account
	if result := r.db.Where("email = ?", email).First(&account); result.Error != nil {
		if result.Error == gorm.ErrRecordNotFound {
			return nil, nil
		}
		return nil, result.Error
	}
	return account, nil
}

func (r AccountDAO) Create(account *domain.Account) (*domain.Account, error) {
	if result := r.db.Create(account); result.Error != nil {
		return nil, result.Error
	}
	return r.FindOne(account.Id)
}

func (r AccountDAO) Delete(id uuid.UUID) error {
	if result := r.db.Delete(&domain.Account{}, id); result.Error != nil {
		return result.Error
	}
	return nil
}

func (r AccountDAO) FindOne(id uuid.UUID) (*domain.Account, error) {
	var account domain.Account
	if result := r.db.Preload(clause.Associations).First(&account, id); result.Error != nil {
		return nil, result.Error
	}
	return &account, nil
}

func (r AccountDAO) FindByToken(token string) (*domain.Account, error) {
	var tokenModel domain.Token
	if result := r.db.Where("token = ?", token).First(&tokenModel); result.Error != nil {
		return nil, result.Error
	}
	return r.FindOne(tokenModel.AccountId)
}

func (r AccountDAO) CreateToken(accountId uuid.UUID, ipAddress string) (*domain.Token, error) {
	token := domain.Token{
		AccountId: accountId,
		IpAddress: ipAddress,
	}
	if result := r.db.Create(&token); result.Error != nil {
		return nil, result.Error
	}
	return &token, nil
}

func (r AccountDAO) RemoveToken(token domain.Token) error {
	result := r.db.Delete(&domain.Token{}, token.ID)
	return result.Error
}

func NewAccountDAO(db gorm.DB) repository.AccountRepository {
	return AccountDAO{db: db}
}
