package config

import (
	"os"
	"strconv"
)

type Config struct {
	ServerUrl string `json:"server_url"`
	Dsn       string `json:"dsn"`
	Port      int    `json:"port"`
}

func (c *Config) LoadFromEnv() error {
	c.ServerUrl = os.Getenv("SERVER_URL")
	c.Dsn = os.Getenv("DSN")
	p, e := strconv.Atoi(os.Getenv("PORT"))
	if e != nil {
		return e
	}
	c.Port = p
	return nil
}
