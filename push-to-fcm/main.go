package main

import (
	"fmt"

	"github.com/gin-gonic/gin"
	"github.com/joho/godotenv"
	"gorm.io/driver/postgres"
	"gorm.io/gorm"
	"systems.panta.milktea/push-to-fcm/pkg/config"
	"systems.panta.milktea/push-to-fcm/pkg/entity"
	"systems.panta.milktea/push-to-fcm/pkg/handler"
	"systems.panta.milktea/push-to-fcm/pkg/root/impl"
)

func main() {
	fmt.Println("Hello, World!")
	err := godotenv.Load(".env")
	if err != nil {
		panic(err)
	}
	config := &config.Config{}
	config.LoadFromEnv()

	db, err := gorm.Open(postgres.Open(config.Dsn), &gorm.Config{})
	if err != nil {
		panic(err)
	}
	db.AutoMigrate(
		&entity.ClientAccount{},
		&entity.PushSubscription{},
	)

	rootModule := impl.NewModule(db, config)
	accountHandler := handler.ClientAccountHandler{
		ServiceModule:    rootModule.GetServiceModule(),
		RepositoryModule: rootModule.GetRepositoryModule(),
	}
	subscriptionHandler := handler.SubscriptionHandler{
		ServiceModule:    rootModule.GetServiceModule(),
		RepositoryModule: rootModule.GetRepositoryModule(),
	}
	r := gin.Default()
	accountHandler.RegisterHandlers(r)
	subscriptionHandler.RegisterHandlers(r)

	r.Run()

}
