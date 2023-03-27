package impl

import (
	"gorm.io/gorm"
	"systems.panta.milktea/push-to-fcm/pkg/config"
	"systems.panta.milktea/push-to-fcm/pkg/dao"
	"systems.panta.milktea/push-to-fcm/pkg/repository"
	"systems.panta.milktea/push-to-fcm/pkg/root"
	"systems.panta.milktea/push-to-fcm/pkg/service"
)

type RootModuleImpl struct {
	DB     *gorm.DB
	Config *config.Config
}

func NewModule(db *gorm.DB, config *config.Config) root.Module {
	return &RootModuleImpl{
		DB:     db,
		Config: config,
	}
}

func (r *RootModuleImpl) GetRepositoryModule() repository.Module {
	return &dao.RepositoryModuleImpl{
		DB: r.DB,
	}

}

func (r *RootModuleImpl) GetServiceModule() service.Module {
	return service.Module{
		RepositoryModule: r.GetRepositoryModule(),
	}
}
