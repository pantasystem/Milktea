package wellknown

import (
	"encoding/json"
	"io"
	"net/http"
)

type WellKnown struct {
	Baseurl string
}

func (r *WellKnown) FindNodeInfo() (*NodeInfo, error) {
	resp, err := http.Get(r.Baseurl + "/.well-known/nodeinfo")
	if err != nil {
		return nil, err
	}
	defer resp.Body.Close()

	body, err := io.ReadAll(resp.Body)
	if err != nil {
		return nil, err
	}

	var wellknown struct {
		Links []struct {
			Rel  string `json:"rel"`
			Href string `json:"href"`
		} `json:"links"`
	}
	err = json.Unmarshal(body, &wellknown)
	if err != nil {
		return nil, err
	}

	var nodeinfoURL string
	for _, link := range wellknown.Links {
		if link.Rel == "http://nodeinfo.diaspora.software/ns/schema/2.0" {
			nodeinfoURL = link.Href
			break
		}
	}

	resp, err = http.Get(nodeinfoURL)
	if err != nil {
		return nil, err
	}
	defer resp.Body.Close()

	body, err = io.ReadAll(resp.Body)
	if err != nil {
		return nil, err
	}

	var ni NodeInfo
	err = json.Unmarshal(body, &ni)
	if err != nil {
		return nil, err
	}

	return &ni, nil
}
