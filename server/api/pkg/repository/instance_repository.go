package repository

import (
	"github.com/google/uuid"
	"systems.panta.milktea/pkg/domain"
)

type InstanceRepository interface {
	Approve(domain.Instance) (*domain.Instance, error)
	Request(domain.Instance) (*domain.Instance, error)
	FindByPublishedInstances() ([]domain.InstanceInfo, error)
	FindById(uuid.UUID) (*domain.Instance, error)
	FindInstanceInfoByHost(host string, isRequirePublished bool) (*domain.InstanceInfo, error)
	Create(domain.Instance) (*domain.Instance, error)
	FindByHost(host string) (*domain.Instance, error)
	FindAll() ([]*domain.Instance, error)
	Update(domain.Instance) error
}
