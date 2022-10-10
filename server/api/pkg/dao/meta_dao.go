package dao

import (
	"encoding/json"
	"fmt"
	"io/ioutil"
	"net/http"

	"gorm.io/gorm"
	"systems.panta.milktea/pkg/domain"
	"systems.panta.milktea/pkg/repository"
)

type MetaDao struct {
	db gorm.DB
}

func (r MetaDao) Sync(host string) (*domain.Meta, error) {
	url := "https://" + host + "/api/meta"
	req, err := http.NewRequest("POST", url, nil)
	req.Header.Set("Content-Type", "application/json")
	client := &http.Client{}
	httpRes, httpErr := client.Do(req)

	if httpErr != nil {
		return nil, httpErr
	}
	if httpRes.StatusCode != 200 {
		return nil, fmt.Errorf("Failed Request meta")
	}

	body, err := ioutil.ReadAll(httpRes.Body)
	if err != nil {

		return nil, err
	}

	var meta MisskeyMeta
	if err := json.Unmarshal(body, &meta); err != nil {

		return nil, err
	}
	return r.Save(domain.Meta{
		Host:        host,
		Name:        meta.Name,
		Description: meta.Description,
		ThemeColor:  meta.ThemeColor,
		BannerUrl:   meta.BannerUrl,
		IconUrl:     meta.IconUrl,
	})

}

func (r MetaDao) FindByHost(host string) (*domain.Meta, error) {
	var meta domain.Meta
	if result := r.db.Where("host = ?", host).
		First(&meta); result.Error != nil {
		return nil, result.Error
	}
	return &meta, nil
}

func (r MetaDao) Save(meta domain.Meta) (*domain.Meta, error) {
	m, err := r.FindByHost(meta.Host)
	if m == nil && err != gorm.ErrRecordNotFound {
		return nil, err
	}

	if m == nil {
		r.db.Create(&meta)

	} else {
		r.db.Save(meta)
	}
	return r.FindByHost(meta.Host)

}
func NewMetaDao(db gorm.DB) repository.MetaRepository {
	return MetaDao{
		db: db,
	}
}

type MisskeyMeta struct {
	Name        *string `json:"name"`
	Description *string `json:"description"`
	ThemeColor  *string `json:"themeColor"`
	BannerUrl   *string `json:"bannerUrl"`
	IconUrl     *string `json:"iconUrl"`
}
