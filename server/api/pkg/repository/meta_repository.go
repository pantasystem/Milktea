package repository

import (
	"systems.panta.milktea/pkg/domain"
)

type MetaRepository interface {
	Sync(string) (*domain.Meta, error)
	FindByHost(string) (*domain.Meta, error)
	Save(domain.Meta) (*domain.Meta, error)
}
