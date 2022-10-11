package config

import (
	"encoding/json"
	"os"
)

type Config struct {
	WebPushAuthSecret string `json:"webPushAuthSecret"`
	WebPushPrivateKey string `json:"webPushPrivateKey"`
	WebPushPublicKey  string `json:"webPushPublicKey"`
}

func LoadConfig() (*Config, error) {
	f, err := os.Open("config/config.json")
	if err != nil {
		return nil, err
	}
	defer f.Close()

	var config Config
	err = json.NewDecoder(f).Decode(&config)
	if err != nil {
		return nil, err
	}
	return &config, err
}
