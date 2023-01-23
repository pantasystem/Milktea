package admin

import (
	"net/http"

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

type UpdateClientMaxBodySizeRequest struct {
	Size int64 `json:"size"`
}

func (r AccountHandler) Setup(engine *gin.Engine) {
	m := DefaultAuthMiddleware{Dao: r.Dao}
	repository := r.Dao.NewAdAccountRepository()

	engine.GET("api/admin/accounts/current", m.CheckToken(), func(c *gin.Context) {
		user, err := GetCurrentUser(&r.Dao, c)
		if user == nil {
			c.JSON(http.StatusInternalServerError, err.Error())
			return
		}
		c.JSON(http.StatusOK, user)
		return
	})

	engine.POST("api/admin/accounts/login", func(c *gin.Context) {
		var req LoginRequest
		if err := c.ShouldBindJSON(&req); err != nil {
			c.JSON(http.StatusBadRequest, err.Error())
			return
		}
		account, err := repository.FindByEmail(req.Email)
		if err != nil {
			c.JSON(http.StatusBadRequest, err.Error())
			return
		}
		result := account.CheckPassword(req.Password)
		if result != nil {
			c.JSON(http.StatusBadRequest, err.Error())
			return
		}
		token, err := repository.CreateToken(account.Id, c.ClientIP())
		if err != nil {
			c.JSON(http.StatusInternalServerError, err.Error())
			return
		}
		c.JSON(http.StatusOK, token)
	})

	engine.POST("api/admin/accounts/logout", m.CheckToken(), func(c *gin.Context) {
		token := *GetToken(c)
		account, err := repository.FindByToken(token)
		if err != nil {
			c.JSON(http.StatusInternalServerError, err.Error())
			return
		}
		if err := repository.RemoveToken(*account.GetToken(token)); err != nil {
			c.JSON(http.StatusInternalServerError, err.Error())
			return
		}
		c.JSON(http.StatusAccepted, nil)
		return

	})

}
