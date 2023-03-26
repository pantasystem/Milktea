package middleware

import (
	"strings"

	"github.com/gin-gonic/gin"
)

type ClientAccountAuthMiddleware struct {
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
