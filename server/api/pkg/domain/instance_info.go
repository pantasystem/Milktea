package domain

type InstanceInfo struct {
	Id                    string  `json:"id"`
	Host                  string  `json:"host"`
	Name                  *string `json:"name"`
	Description           *string `json:"description"`
	ClientMaxBodyByteSize *int64  `json:"clientMaxBodyByteSize"`
	IconUrl               *string `json:"iconUrl"`
	ThemeColor            *string `json:"themeColor"`
}
