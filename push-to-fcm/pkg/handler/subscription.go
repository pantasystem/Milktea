package handler

import (
	"fmt"

	"github.com/gin-gonic/gin"
	"systems.panta.milktea/push-to-fcm/pkg/entity"
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
		var req SubscribeRequest
		err := c.BindJSON(&req)
		if err != nil {
			c.Status(400)
			return
		}
		if req.ProviderType != entity.ProviderTypeMisskey && req.ProviderType != entity.ProviderTypeMastodon {
			c.Status(400)
			return
		}
		a, exists := c.Get(middleware.CLIENT_ACCOUNT)
		if !exists {
			fmt.Printf("account fetch failed\n")
			c.Status(500)
			return
		}
		ac, ok := a.(*entity.ClientAccount)
		if !ok {
			fmt.Printf("account cast failed\n")
			c.Status(500)
			return
		}
		sub, err := r.ServiceModule.GetPushNotificationService().Subscribe(c, ac.ID, service.SubscribeArgs{
			ProviderType: req.ProviderType,
			Acct:         req.Acct,
			InstanceUri:  req.InstanceUri,
			Token:        req.Token,
		})
		if err != nil {
			fmt.Printf("err: %v\n", err)
			c.Status(500)
			return
		}
		c.JSON(200, sub)
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

type SubscribeRequest struct {
	ProviderType entity.ProviderType `json:"provider_type"`
	Acct         string              `json:"acct"`
	InstanceUri  string              `json:"instance_uri"`
	Token        string              `json:"token"`
}
