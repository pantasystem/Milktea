package main

import (
	"fmt"
	"net/http"

	"github.com/gin-gonic/gin"
	"systems.panta.milktea/pkg/dao"
	"systems.panta.milktea/pkg/handler"
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

	instanceHnadler.Setup(engine)
	engine.Run(":8080")
}
