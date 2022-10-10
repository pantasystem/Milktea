package dao

import (
	"gorm.io/driver/postgres"
	"gorm.io/gorm"
	"systems.panta.milktea/pkg/domain"
	"systems.panta.milktea/pkg/repository"
)

type Dao struct {
	db *gorm.DB
}

func Init() Dao {
	dsn := "host=psql user=dbuser password=secret dbname=database port=5432 sslmode=disable TimeZone=Asia/Tokyo"

	db, err := gorm.Open(postgres.Open(dsn))
	if err != nil {
		panic("db connection error")
	}
	if err := db.AutoMigrate(&domain.Instance{}); err != nil {
		panic(err)
	}
	return Dao{
		db: db,
	}
}

func (r *Dao) NewInstanceRepository() repository.InstanceRepository {
	return NewInstanceRepository(*r.db)
}
