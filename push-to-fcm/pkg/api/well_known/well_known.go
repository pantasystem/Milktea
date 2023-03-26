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
		NodeInfoURL string `json:"nodeinfo"`
	}
	err = json.Unmarshal(body, &wellknown)
	if err != nil {
		return nil, err
	}

	resp, err = http.Get(wellknown.NodeInfoURL)
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
