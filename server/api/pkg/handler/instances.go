package handler

import (
	"net/http"
	"time"

	"github.com/gin-gonic/gin"
	"gorm.io/gorm"
	"systems.panta.milktea/pkg/dao"
	"systems.panta.milktea/pkg/domain"
)

type InstanceHandler struct {
	Dao dao.Dao
}

type CreateInstanceRequest struct {
	Host string `json:"host"`
}

type MisskeyMeta struct {
	Name        *string `json:"name"`
	Description *string `json:"description"`
	ThemeColor  *string `json:"themeColor"`
	BannerUrl   *string `json:"bannerUrl"`
	IconUrl     *string `json:"iconUrl"`
}

func (r InstanceHandler) Setup(e *gin.Engine) {
	e.POST("/api/instances", func(c *gin.Context) {

		var jsonReq CreateInstanceRequest
		if err := c.ShouldBindJSON(&jsonReq); err != nil {
			c.JSON(http.StatusBadRequest, err.Error())
			return
		}

		instance, err := r.Dao.NewInstanceRepository().FindByHost(jsonReq.Host)

		if err != nil && err != gorm.ErrRecordNotFound {
			c.Status(500)
			return
		}

		if instance == nil || (time.Now().Unix()-instance.UpdatedAt.Unix()) >= 24*60*60*1000 {
			_, err := r.Dao.NewMetaRepository().Sync(jsonReq.Host)

			if err != nil {
				c.JSON(http.StatusBadRequest, err.Error())
				return
			}
		}

		res, err := r.Dao.NewInstanceRepository().Request(domain.Instance{
			Host: jsonReq.Host,
		})

		if err != nil {
			c.JSON(http.StatusInternalServerError, "Error")
			return
		}
		if res != nil {
			c.JSON(http.StatusOK, instance)
		}

	})

	e.GET("/api/instances", func(c *gin.Context) {
		instances, err := r.Dao.NewInstanceRepository().FindByPublishedInstances()
		if err != nil {
			c.JSON(http.StatusInternalServerError, err.Error())
			return
		}
		c.JSON(http.StatusOK, instances)

	})
}
