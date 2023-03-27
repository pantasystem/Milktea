package root

import "systems.panta.milktea/push-to-fcm/pkg/repository"

type Module interface {
	GetRepositoryModule() repository.Module
}
