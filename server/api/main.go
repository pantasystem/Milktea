package main

import (
	"fmt"
	"net/http"

	"github.com/gin-gonic/gin"
	"systems.panta.milktea/pkg/dao"
	"systems.panta.milktea/pkg/handler"
	"systems.panta.milktea/pkg/handler/admin"
)

func main() {
	fmt.Printf("Test")
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
	instanceHnadler.Setup(engine)
	adminAccountHandler.Setup(engine)

	engine.Run(":8080")
}
