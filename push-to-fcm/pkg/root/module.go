package root

import (
	"systems.panta.milktea/push-to-fcm/pkg/repository"
	"systems.panta.milktea/push-to-fcm/pkg/service"
)

type Module interface {
	GetRepositoryModule() repository.Module
	GetServiceModule() service.Module
}
