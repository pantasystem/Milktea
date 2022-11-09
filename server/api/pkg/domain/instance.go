package domain

import (
	"time"

	"github.com/google/uuid"
	"gorm.io/gorm"
)

type Instance struct {
	Id                    uuid.UUID  `json:"id" gorm:"primaryKey"`
	Host                  string     `json:"host" gorm:"unique"`
	PublishedAt           *time.Time `json:"publishedAt"`
	ClientMaxBodyByteSize *int64     `json:"clientMaxBodyByteSize"`
	DeletedAt             *time.Time `json:"deletedAt"`
	CreatedAt             time.Time  `json:"createdAt"`
	UpdatedAt             time.Time  `json:"updatedAt"`
}

func (r *Instance) Approve() {
	now := time.Now()
	r.PublishedAt = &now
}

func (r *Instance) BeforeCreate(tx *gorm.DB) (err error) {
	uuid, err := uuid.NewRandom()
	r.Id = uuid
	return err
}
