package main

import (
	"fmt"
	"net/http"

	"github.com/gin-gonic/gin"
	"systems.panta.milktea/pkg/config"
	"systems.panta.milktea/pkg/dao"
	"systems.panta.milktea/pkg/domain"
	"systems.panta.milktea/pkg/handler"
	"systems.panta.milktea/pkg/handler/admin"
)

func main() {
	fmt.Printf("Test")
	config, err := config.LoadConfig()

	if err != nil {
		panic(err)
	}

	d := dao.Init()
	engine := gin.Default()

	engine.GET("/api/ping", func(c *gin.Context) {
		c.JSON(http.StatusOK, gin.H{
			"message": "pongggg",
		})
	})
	instanceHnadler := handler.InstanceHandler{
		Dao: d,
	}

	adminAccountHandler := admin.AccountHandler{
		Dao: d,
	}

	adminInstanceHandler := admin.AdminInstanceHandler{
		Dao: d,
	}

	decrypter, err := domain.NewDecrypter(config.WebPushAuthSecret, config.WebPushPublicKey, config.WebPushPrivateKey)
	if err != nil {
		panic(err)
	}

	pushToFcmHandler := handler.PushToFCMHandler{
		Decrypter: *decrypter,
		Dao:       d,
	}

	instanceHnadler.Setup(engine)
	adminAccountHandler.Setup(engine)
	pushToFcmHandler.Setup(engine)
	adminInstanceHandler.Setup(engine)

	engine.Run(":8080")
}
