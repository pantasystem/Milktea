package handler

import (
	"github.com/gin-gonic/gin"
	"systems.panta.milktea/push-to-fcm/pkg/repository"
	"systems.panta.milktea/push-to-fcm/pkg/service"
)

type ClientAccountHandler struct {
	RepositoryModule repository.Module
	ServiceModule    service.Module
}

func (r *ClientAccountHandler) RegisterHandlers(gin *gin.Engine) {
	gin.POST("api/client-account/get-or-register", r.GetOrRegisterClientAccount())
	gin.PUT("api/client-account", r.UpdateClientAccount())
}

func (c *ClientAccountHandler) GetOrRegisterClientAccount() gin.HandlerFunc {
	return func(c *gin.Context) {
	}
}

func (c *ClientAccountHandler) UpdateClientAccount() gin.HandlerFunc {
	return func(c *gin.Context) {
	}
}
