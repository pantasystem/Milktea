package middleware

import (
	"net/http"
	"strings"

	"github.com/gin-gonic/gin"
	"systems.panta.milktea/push-to-fcm/pkg/repository"
)

type ClientAccountAuthMiddleware struct {
	RepositoryModule repository.Module
}

func (r *ClientAccountAuthMiddleware) GetTokenWithAccount() gin.HandlerFunc {
	return func(c *gin.Context) {
		token := GetToken(c)
		if token == nil {
			c.Next()
			return
		}
		account, err := r.RepositoryModule.GetClientAccountRepository().FindByToken(c, *token)
		if account != nil && err == nil {
			c.Set("client_account", account)
			c.Set("client_account_id", account.ID)
			c.Next()
			return
		} else {
			c.Next()
			return
		}
	}
}

func (r *ClientAccountAuthMiddleware) CheckToken() gin.HandlerFunc {
	return func(c *gin.Context) {
		token := GetToken(c)
		if token == nil {
			c.JSON(http.StatusUnauthorized, gin.H{"type": "unauthorized", "message": "invalid token"})
			c.Abort()
			return
		}
		account, err := r.RepositoryModule.GetClientAccountRepository().FindByToken(c, *token)
		if account != nil && err == nil {
			c.Set("client_account", account)
			c.Set("client_account_id", account.ID)
			c.Next()
			return
		} else {
			c.JSON(http.StatusUnauthorized, gin.H{"type": "unauthorized", "message": "invalid token"})
			c.Abort()
			return
		}
	}
}

func GetToken(c *gin.Context) *string {
	aHeader := c.Request.Header["Authorization"]
	if len(aHeader) < 1 {
		return nil
	}
	sliced := strings.Split(aHeader[0], " ")
	if len(sliced) <= 1 {
		return nil
	}

	token := sliced[1]
	return &token
}
