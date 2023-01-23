package domain

type Meta struct {
	Host        string  `json:"host" gorm:"primaryKey"`
	Name        *string `json:"name"`
	Description *string `json:"description"`
	ThemeColor  *string `json:"themeColor"`
	BannerUrl   *string `json:"bannerUrl"`
	IconUrl     *string `json:"iconUrl"`
}
