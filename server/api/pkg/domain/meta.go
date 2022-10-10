package domain

import "github.com/google/uuid"

type Meta struct {
	InstanceId  uuid.UUID `json:"instanceId" gorm:"primaryKey"`
	Name        *string   `json:"name"`
	Description *string   `json:"description"`
	ThemeColor  *string   `json:"themeColor"`
	BannerUrl   *string   `json:"bannerUrl"`
	IconUrl     *string   `json:"iconUrl"`
	Instance    Instance  `json:"instance"`
}
