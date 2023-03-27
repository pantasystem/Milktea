package handler

import (
	"github.com/gin-gonic/gin"
	"systems.panta.milktea/push-to-fcm/pkg/handler/middleware"
	"systems.panta.milktea/push-to-fcm/pkg/repository"
	"systems.panta.milktea/push-to-fcm/pkg/service"
)

type SubscriptionHandler struct {
	ServiceModule    service.Module
	RepositoryModule repository.Module
}

func (r *SubscriptionHandler) RegisterHandlers(gin *gin.Engine) {
	m := middleware.ClientAccountAuthMiddleware{
		RepositoryModule: r.RepositoryModule,
	}
	gin.POST("api/subscriptions", m.CheckToken(), r.Subscribe())
	gin.DELETE("api/subscriptions", m.CheckToken(), r.Unsubscribe())
	gin.POST("api/subscriptions/:id/callbacks", r.OnRecieveNotification())
}

func (r *SubscriptionHandler) Subscribe() gin.HandlerFunc {
	return func(c *gin.Context) {
	}
}

func (r *SubscriptionHandler) Unsubscribe() gin.HandlerFunc {
	return func(c *gin.Context) {
	}
}

func (r *SubscriptionHandler) OnRecieveNotification() gin.HandlerFunc {
	return func(c *gin.Context) {
	}
}
