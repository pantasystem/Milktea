package config

type Config struct {
	ServerUrl string `json:"server_url"`
	Dsn       string `json:"dsn"`
	Port      int    `json:"port"`
}
