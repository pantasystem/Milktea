package handler

import (
	"net/http"

	"github.com/gin-gonic/gin"
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

		meta, err := r.Dao.NewMetaRepository().Sync(jsonReq.Host)

		if err != nil {
			c.JSON(http.StatusBadRequest, err.Error())
			return
		}

		res, err := r.Dao.NewInstanceRepository().Request(domain.Instance{
			Host: jsonReq.Host,
		})

		if err != nil {
			c.JSON(http.StatusInternalServerError, "Error")
			return
		}
		if res != nil {
			c.JSON(http.StatusOK, meta)
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
