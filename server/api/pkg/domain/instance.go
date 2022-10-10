package domain

import (
	"time"

	"github.com/google/uuid"
)

type Instance struct {
	Id          uuid.UUID  `json:"id" gorm:"primaryKey"`
	Host        string     `json:"host" gorm:"unique"`
	PublishedAt *time.Time `json:"publishedAt"`
	DeletedAt   *time.Time `json:"deletedAt"`
	CreatedAt   time.Time  `json:"createdAt"`
	UpdatedAt   time.Time  `json:"updatedAt"`
}

func (r *Instance) Approve() {
	now := time.Now()
	r.PublishedAt = &now
}
