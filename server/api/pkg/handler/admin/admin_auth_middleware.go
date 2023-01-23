package admin

import (
	"net/http"
	"strings"

	"github.com/gin-gonic/gin"
	"systems.panta.milktea/pkg/dao"
	"systems.panta.milktea/pkg/domain"
)

type DefaultAuthMiddleware struct {
	Dao dao.Dao
}

func (r *DefaultAuthMiddleware) CheckToken() gin.HandlerFunc {
	return func(c *gin.Context) {
		token := GetToken(c)
		if token == nil {
			c.JSON(http.StatusUnauthorized, gin.H{"type": "unauthorized", "message": "invalid token"})
			c.Abort()
			return
		}
		user, err := r.Dao.NewAdAccountRepository().FindByToken(*token)
		if user != nil && err == nil {
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

func GetCurrentUser(dao *dao.Dao, c *gin.Context) (*domain.AdAccount, error) {
	token := GetToken(c)
	if token == nil {
		return nil, nil
	}
	return dao.NewAdAccountRepository().FindByToken(*token)
}
