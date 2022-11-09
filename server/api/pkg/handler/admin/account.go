package admin

import (
	"net/http"

	"github.com/gin-gonic/gin"
	"github.com/google/uuid"
	"gorm.io/gorm"
	"systems.panta.milktea/pkg/dao"
	"systems.panta.milktea/pkg/domain"
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
	instanceRepository := r.Dao.NewInstanceRepository()

	engine.POST("/api/admin/accounts/login", func(c *gin.Context) {
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

	engine.GET("/api/admin/instances", m.CheckToken(), func(c *gin.Context) {
		instances, err := instanceRepository.FindAll()
		if err != nil {
			c.JSON(http.StatusInternalServerError, err.Error())
			return
		}
		c.JSON(http.StatusOK, instances)
	})

	engine.POST("/api/admin/instances/:instanceId/approve", m.CheckToken(), func(c *gin.Context) {
		instanceId := c.Params.ByName("instanceId")
		id, err := uuid.Parse(instanceId)
		if err != nil {
			c.JSON(http.StatusBadRequest, err.Error())
			return
		}
		instance, err := instanceRepository.FindById(id)
		if err == gorm.ErrRecordNotFound {
			c.JSON(http.StatusNotFound, err.Error())
			return
		}
		if err != nil || instance == nil {
			c.JSON(http.StatusInternalServerError, err.Error())
			return
		}

		instance, err = instanceRepository.Approve(*instance)
		if err != nil {
			c.JSON(http.StatusInternalServerError, err.Error())
			return
		}
		c.JSON(http.StatusOK, instance)
	})

	engine.POST("/api/admin/instances/:instanceId/update-client-max-body-size", m.CheckToken(), func(c *gin.Context) {
		var req UpdateClientMaxBodySizeRequest

		err := c.ShouldBindJSON(&req)
		if err != nil {
			c.JSON(http.StatusBadRequest, err.Error())
			return
		}

		instanceId := c.Params.ByName("instanceId")
		id, err := uuid.Parse(instanceId)
		if err != nil {
			c.JSON(http.StatusBadRequest, err.Error())
			return
		}

		instance, conti := r.GetInstance(c, id)
		if !conti {
			return
		}
		instance.ClientMaxBodyByteSize = &req.Size
		instanceRepository.Update(*instance)
		c.Status(http.StatusAccepted)
		return
	})

}

func (r AccountHandler) GetInstance(c *gin.Context, instanceId uuid.UUID) (*domain.Instance, bool) {
	instance, err := r.Dao.NewInstanceRepository().FindById(instanceId)
	if err != nil {
		if err == gorm.ErrRecordNotFound {
			c.JSON(http.StatusNotFound, err.Error())
			return nil, false
		} else {
			c.JSON(http.StatusInternalServerError, err.Error())
			return nil, false
		}
	}
	return instance, true
}
