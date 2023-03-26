package dao

import "gorm.io/gorm"

type ClientAccountRepositoryImpl struct {
	DB *gorm.DB
}
