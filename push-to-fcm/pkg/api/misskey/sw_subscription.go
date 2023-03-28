package misskey

import (
	"bytes"
	"context"
	"encoding/json"
	"errors"
	"fmt"
	"io"
	"net/http"
)

type State string

const (
	StateAlreadySubscribed State = "already-subscribed"
	StateSubscribed        State = "subscribed"
)

type SWSubscription struct {
	BaseUrl string
	Token   string
}

type SubscribeRequest struct {
	Endpoint  string `json:"endpoint"`
	Auth      string `json:"auth"`
	PublicKey string `json:"publicKey"`
}

type InternalSubscribeRequest struct {
	I         string `json:"i"`
	Endpoint  string `json:"endpoint"`
	Auth      string `json:"auth"`
	PublicKey string `json:"publickey"`
}

type InternalUnSubRequest struct {
	I        string `json:"i"`
	EndPoint string `json:"endpoint"`
}
type SubscribeResponse struct {
	State State   `json:"state"`
	Key   *string `json:"key"`
}

func (r *SWSubscription) Subscribe(ctx context.Context, req SubscribeRequest) (*SubscribeResponse, error) {
	rqi := InternalSubscribeRequest{
		I:         r.Token,
		Endpoint:  req.Endpoint,
		Auth:      req.Auth,
		PublicKey: req.PublicKey,
	}
	rejn, err := json.Marshal(rqi)
	if err != nil {
		return nil, err
	}
	c := &http.Client{}
	hr, err := http.NewRequest("POST", r.BaseUrl+"/api/sw/register", bytes.NewReader(rejn))
	if err != nil {
		return nil, err
	}
	hr.Header.Add("Content-Type", "application/json")
	res, err := c.Do(hr)
	if err != nil {
		return nil, err
	}
	defer res.Body.Close()
	rwb, err := io.ReadAll(res.Body)
	if err != nil {
		return nil, err
	}

	if res.StatusCode >= 400 {
		fmt.Printf("misskey subscribe error: %s\n", rwb)
		return nil, errors.New(res.Status)
	}

	var sr SubscribeResponse
	err = json.Unmarshal(rwb, &sr)
	if err != nil {
		return nil, err
	}
	return &sr, nil
}

func (r *SWSubscription) Unsubscribe(ctx context.Context, endpoint string) error {
	rqi := InternalUnSubRequest{
		I:        r.Token,
		EndPoint: endpoint,
	}
	rejn, err := json.Marshal(rqi)
	if err != nil {
		return err
	}
	c := &http.Client{}
	hr, err := http.NewRequest("POST", r.BaseUrl+"/api/sw/unregister", bytes.NewReader(rejn))
	if err != nil {
		return err
	}
	hr.Header.Add("Content-Type", "application/json")
	res, err := c.Do(hr)
	if err != nil {
		return err
	}
	defer res.Body.Close()
	if res.StatusCode >= 400 {
		return errors.New(res.Status)
	}

	return nil
}
