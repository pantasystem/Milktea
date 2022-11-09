package dao

import (
	"fmt"

	"github.com/google/uuid"
	"gorm.io/gorm"
	"systems.panta.milktea/pkg/domain"
	"systems.panta.milktea/pkg/repository"
)

type InstanceDao struct {
	db gorm.DB
}

func (r InstanceDao) Approve(instance domain.Instance) (*domain.Instance, error) {
	i, err := r.FindById(instance.Id)
	if err != nil {
		return nil, err
	}
	i.Approve()
	if result := r.db.Model(&domain.Instance{}).
		Where("id = ?", instance.Id).
		Updates(map[string]interface{}{
			"PublishedAt": i.PublishedAt,
		}); result.Error != nil {
		return nil, result.Error
	}

	return r.FindById(instance.Id)
}

func (r InstanceDao) Request(instance domain.Instance) (*domain.Instance, error) {
	i, err := r.FindByHost(instance.Host)
	if err != nil && err != gorm.ErrRecordNotFound {
		return nil, err
	}
	fmt.Printf("第一段階突破")
	if i == nil || err == gorm.ErrRecordNotFound {
		instance.PublishedAt = nil
		return r.Create(instance)
	} else {
		return i, err
	}

}

func (r InstanceDao) FindByPublishedInstances() ([]domain.InstanceInfo, error) {
	var list []domain.InstanceInfo
	if result := r.db.
		Table("instances").
		Select(
			"instances.host",
			"meta.name", "meta.description",
			"instances.client_max_body_byte_size",
			"meta.icon_url",
			"meta.theme_color",
		).
		Where("instances.published_at is not null").
		Where("instances.deleted_at is null").
		Joins("LEFT JOIN meta ON instances.host = meta.host").
		Find(&list); result.Error != nil {
		return nil, result.Error
	}
	return list, nil

}

func (r InstanceDao) FindById(instanceId uuid.UUID) (*domain.Instance, error) {
	var instance *domain.Instance
	if result := r.db.First(&instance, instanceId); result.Error != nil {
		return nil, result.Error
	}
	return instance, nil
}

func (r InstanceDao) Create(instance domain.Instance) (*domain.Instance, error) {
	if result := r.db.Create(&instance); result.Error != nil {
		return nil, result.Error
	}

	return r.FindById(instance.Id)
}

func (r InstanceDao) FindByHost(host string) (*domain.Instance, error) {
	var instance domain.Instance
	if result := r.db.Where("host = ?", host).First(&instance); result.Error != nil {
		return nil, result.Error
	}
	return &instance, nil
}

func (r InstanceDao) FindAll() ([]*domain.Instance, error) {
	var instances []*domain.Instance
	if result := r.db.Find(&instances); result.Error != nil {
		return nil, result.Error
	}
	return instances, nil
}

func (r InstanceDao) Update(instance domain.Instance) error {

	if result := r.db.Model(&domain.Instance{}).
		Where("id = ?", instance.Id).
		Updates(map[string]interface{}{
			"ClientMaxBodyByteSize": instance.ClientMaxBodyByteSize,
			"PublishedAt":           instance.PublishedAt,
		}); result.Error != nil {
		return result.Error
	}
	return nil
}

func NewInstanceRepository(db gorm.DB) repository.InstanceRepository {
	return InstanceDao{db: db}
}
