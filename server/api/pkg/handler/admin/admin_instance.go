package admin

import (
	"net/http"

	"github.com/gin-gonic/gin"
	"github.com/google/uuid"
	"gorm.io/gorm"
	"systems.panta.milktea/pkg/dao"
	"systems.panta.milktea/pkg/domain"
)

type AdminInstanceHandler struct {
	Dao dao.Dao
}

type CreateInstanceRequest struct {
	Host      string `json:"host"`
	IsPublish bool   `json:"isPublish"`
}

func (r *AdminInstanceHandler) Setup(engine *gin.Engine) {
	m := DefaultAuthMiddleware{Dao: r.Dao}
	instanceRepository := r.Dao.NewInstanceRepository()

	engine.GET("api/admin/instances", m.CheckToken(), func(c *gin.Context) {
		instances, err := instanceRepository.FindAll()
		if err != nil {
			c.JSON(http.StatusInternalServerError, err.Error())
			return
		}
		c.JSON(http.StatusOK, instances)
	})

	engine.POST("api/admin/instances", m.CheckToken(), func(c *gin.Context) {
		var req CreateInstanceRequest
		if err := c.ShouldBindJSON(&req); err != nil {
			c.Status(400)
			return
		}

		_, err := r.Dao.NewMetaRepository().Sync(req.Host)
		if err != nil {
			c.Status(400)
			return
		}

		instance, err := instanceRepository.Request(domain.Instance{
			Host: req.Host,
		})
		if err != nil {
			c.Status(400)
			return
		}

		if req.IsPublish {
			instance, err = instanceRepository.Approve(*instance)
		}

		if err != nil {
			c.Status(500)
			return
		}
		c.JSON(http.StatusOK, instance)
	})

	engine.GET("api/admin/instances/:instanceId", m.CheckToken(), func(c *gin.Context) {
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
		c.JSON(http.StatusOK, instance)
		return
	})

	engine.GET("api/admin/instance-with-meta/:host", m.CheckToken(), func(c *gin.Context) {
		host := c.Params.ByName("host")
		instance, err := instanceRepository.FindInstanceInfoByHost(host, false)
		if err != nil {
			if err == gorm.ErrRecordNotFound {
				c.Status(http.StatusNotFound)
				return
			} else {
				c.Status(http.StatusInternalServerError)
				return
			}
		}
		c.JSON(http.StatusOK, instance)
		return
	})

	engine.POST("api/admin/instances/:instanceId/approve", m.CheckToken(), func(c *gin.Context) {
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

	engine.POST("api/admin/instances/:instanceId/update-client-max-body-size", m.CheckToken(), func(c *gin.Context) {
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

func (r AdminInstanceHandler) GetInstance(c *gin.Context, instanceId uuid.UUID) (*domain.Instance, bool) {
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
