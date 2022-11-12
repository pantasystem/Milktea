package handler

import (
	"encoding/json"
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

type NotifyMsg struct {
	Type  string
	Title string
	Body  string
}

type RemoteUserDTO struct {
	Name     *string `json:"name"`
	UserName string  `json:"username"`
}

type RemoteNoteDTO struct {
	Text  *string          `json:"text"`
	CW    *string          `json:"cw"`
	Files *[]RemoteFileDTO `json:"files"`
	Poll  *RemotePollDTO   `json:"poll"`
}
type RemoteNotifyDTO struct {
	Type string         `json:"type"`
	Id   string         `json:"id"`
	User *RemoteUserDTO `json:"user"`
	Note *RemoteNoteDTO `json:"note"`
}

type RemoteFileDTO struct {
	Id string `json:"id"`
}

type RemotePollDTO struct {
	Choices *[]interface{} `json:"choices"`
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
			ctx.Status(400)
		}

		decryptedBody, err := r.Decrypter.Decrypt(string(body))
		if err != nil {
			fmt.Printf("decrypt error:%s", err.Error())
			ctx.Status(400)
			return
		}
		var jsonObj map[string]interface{}
		if json.Unmarshal([]byte(*decryptedBody), &jsonObj); err != nil {
			fmt.Printf("JSONデコードに失敗したためキャンセル:%s", *decryptedBody)
			ctx.Status(400)
			return
		}

		t := jsonObj["type"]
		if t != "notification" {
			fmt.Printf("通知ではないためキャンセル:%s", *decryptedBody)
			ctx.Status(400)
			return
		}

		// r.GenerateNotifyMsg(jsonObj)
		ctx.JSON(http.StatusOK, decryptedBody)
	})
}

// func (r PushToFCMHandler) GenerateNotifyMsg(jsonObj map[string]interface{}) (*NotifyMsg, error) {

// }
