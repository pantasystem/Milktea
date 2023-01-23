package handler

import (
	"fmt"
	"io/ioutil"
	"net/http"

	"github.com/gin-gonic/gin"
	"systems.panta.milktea/pkg/dao"
	"systems.panta.milktea/pkg/domain"
)

type PushToFCMHandler struct {
	Dao       dao.Dao
	Decrypter domain.Decrypter
}

func (r PushToFCMHandler) Setup(e *gin.Engine) {
	e.POST("/webpushcallback", func(ctx *gin.Context) {
		deviceToken, dtErr := ctx.GetQuery("deviceToken")
		accountId, acErr := ctx.GetQuery("accountId")
		if !dtErr || !acErr {
			ctx.Status(410)
			return
		}

		fmt.Printf("deviceToken:%s, accountId:%s", deviceToken, accountId)
		body, err := ioutil.ReadAll(ctx.Request.Body)
		if err != nil {
			ctx.Status(500)
		}

		decryptedBody, err := r.Decrypter.Decrypt(string(body))
		if err != nil {
			fmt.Printf("decrypt error:%s", err.Error())
			ctx.Status(500)
		}
		ctx.JSON(http.StatusOK, decryptedBody)
	})
}
