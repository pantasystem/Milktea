package handler

import (
	"github.com/gin-gonic/gin"
	"systems.panta.milktea/push-to-fcm/pkg/entity"
	"systems.panta.milktea/push-to-fcm/pkg/handler/middleware"
	"systems.panta.milktea/push-to-fcm/pkg/repository"
	"systems.panta.milktea/push-to-fcm/pkg/service"
)

type ClientAccountHandler struct {
	RepositoryModule repository.Module
	ServiceModule    service.Module
}

func (r *ClientAccountHandler) RegisterHandlers(gin *gin.Engine) {
	m := middleware.ClientAccountAuthMiddleware{
		RepositoryModule: r.RepositoryModule,
	}
	gin.POST("api/client-account/get-or-register", r.GetOrRegisterClientAccount())
	gin.PUT("api/client-account", m.CheckToken(), r.UpdateClientAccount())
}

func (r *ClientAccountHandler) GetOrRegisterClientAccount() gin.HandlerFunc {
	return func(c *gin.Context) {
		t := middleware.GetToken(c)
		ac, err := r.ServiceModule.GetClientAccountService().GetOrRegisterClientAccount(c, t)
		if err != nil {
			c.Status(500)
			return
		}
		res := GetOrRegisterClientAccountRes{
			Token:   ac.Token,
			Account: ac,
		}
		c.JSON(200, res)
	}
}

func (r *ClientAccountHandler) UpdateClientAccount() gin.HandlerFunc {
	return func(c *gin.Context) {
		c.Value(middleware.IS_AUTHENTICATED)
		a, exists := c.Get(middleware.CLIENT_ACCOUNT)
		if !exists {
			c.Status(500)
			return
		}
		ac, ok := a.(*entity.ClientAccount)
		if !ok {
			c.Status(500)
			return
		}
		var req UpdateClientAccountReq
		err := c.BindJSON(&req)
		if err != nil {
			c.Status(400)
			return
		}

		if req.DeviceToken != nil && len(*req.DeviceToken) > 0 {
			ac.DeviceToken = req.DeviceToken
		}

		if req.Lang != nil && len(*req.Lang) > 0 {
			ac.Lang = req.Lang
		}
		ac, err = r.RepositoryModule.GetClientAccountRepository().Update(c, ac)
		if err != nil {
			c.Status(500)
			return
		}

		c.JSON(200, ac)
	}
}

type GetOrRegisterClientAccountRes struct {
	Token   string                `json:"token"`
	Account *entity.ClientAccount `json:"account"`
}

type UpdateClientAccountReq struct {
	DeviceToken *string `json:"device_token"`
	Lang        *string `json:"lang"`
}
