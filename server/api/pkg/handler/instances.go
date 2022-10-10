package handler

import (
	"io/ioutil"
	"net/http"

	"encoding/json"

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
		res, err := r.Dao.NewInstanceRepository().Request(domain.Instance{
			Host: jsonReq.Host,
		})

		url := "https://" + jsonReq.Host + "/api/meta"
		req, err := http.NewRequest("POST", url, nil)
		req.Header.Set("Content-Type", "application/json")
		client := &http.Client{}
		httpRes, httpErr := client.Do(req)

		if httpErr != nil {
			c.JSON(http.StatusInternalServerError, httpErr.Error())
			return
		}
		if httpRes.StatusCode != 200 {
			c.JSON(http.StatusBadRequest, "Invalid Host name")
			return
		}

		body, err := ioutil.ReadAll(httpRes.Body)
		if err != nil {
			c.JSON(http.StatusInternalServerError, err.Error())
			return
		}

		var meta MisskeyMeta
		if err := json.Unmarshal(body, &meta); err != nil {
			c.JSON(http.StatusInternalServerError, "Error")
			return
		}

		if err != nil {
			c.JSON(http.StatusInternalServerError, "Error")
			return
		}
		if res != nil {
			c.JSON(http.StatusOK, meta)
		}

	})
}
