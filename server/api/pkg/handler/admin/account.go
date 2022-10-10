package admin

import (
	"github.com/gin-gonic/gin"
	"systems.panta.milktea/pkg/dao"
)

type AccountHandler struct {
	Dao dao.Dao
}

type LoginRequest struct {
	Email    string `json:"email"`
	Password string `json:"password"`
}

func (r AccountHandler) Setup(engine *gin.Engine) {
	m := DefaultAuthMiddleware{Dao: r.Dao}

	engine.POST("/api/admin/accounts/login", func(c *gin.Context) {

	})

	engine.POST("api/admin/accounts/logout", m.CheckToken(), func(c *gin.Context) {

	})

	engine.GET("/api/admin/instances", m.CheckToken(), func(c *gin.Context) {

	})
	engine.POST("/api/admin/instances/:instanceId/approve", m.CheckToken(), func(c *gin.Context) {
		// instanceId := c.Params.ByName("instanceId")

	})

}
