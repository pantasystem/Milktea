package mastodon

import (
	"bytes"
	"context"
	"encoding/json"
	"errors"
	"io"
	"net/http"
)

type NotificationSubscriptionClient struct {
	BaseUrl string
	Token   string
}

func (r *NotificationSubscriptionClient) Subscribe(ctx context.Context, req PushSubscriptionRequest) (*PushSubscription, error) {
	rj, err := json.Marshal(req)
	if err != nil {
		return nil, err
	}
	c := &http.Client{}
	request, err := http.NewRequest("POST", r.BaseUrl+"/api/v1/push/subscription", bytes.NewReader(rj))
	if err != nil {
		return nil, err
	}

	request.Header.Add("Content-Type", "application/json")
	if r.Token != "" {
		request.Header.Add("Authorization", "Bearer "+r.Token)
	}

	res, err := c.Do(request)
	if err != nil {
		return nil, err
	}
	defer res.Body.Close()

	if res.StatusCode >= 400 {
		return nil, errors.New(res.Status)
	}

	body, err := io.ReadAll(res.Body)
	if err != nil {
		return nil, err
	}
	var ps PushSubscription

	err = json.Unmarshal(body, &ps)
	if err != nil {
		return nil, err
	}
	return &ps, nil
}

type PushSubscription struct {
	ID        int64  `json:"id"`
	Endpoint  string `json:"endpoint"`
	ServerKey string `json:"server_key"`
	Alerts    struct {
		Mention       bool `json:"mention,omitempty"`
		Status        bool `json:"status,omitempty"`
		Reblog        bool `json:"reblog,omitempty"`
		Follow        bool `json:"follow,omitempty"`
		FollowRequest bool `json:"follow_request,omitempty"`
		Favourite     bool `json:"favourite,omitempty"`
		Poll          bool `json:"poll,omitempty"`
		Update        bool `json:"update,omitempty"`
		AdminSignUp   bool `json:"admin.sign_up,omitempty"`
		AdminReport   bool `json:"admin.report,omitempty"`
	} `json:"alerts,omitempty"`
}

type PushSubscriptionRequest struct {
	Subscrption Subscrption `json:"subscription,omitempty"`
	Data        Data        `json:"data,omitempty"`
}

type Subscrption struct {
	Endpoint string `json:"endpoint"`
	Keys     Keys   `json:"keys"`
}

type Keys struct {
	P256dh string `json:"p256dh"`
	Auth   string `json:"auth"`
}

type Data struct {
	Alerts *Alerts `json:"alerts,omitempty"`
	Policy string  `json:"policy,omitempty"`
}

type Alerts struct {
	Mention       bool `json:"mention,omitempty"`
	Status        bool `json:"status,omitempty"`
	Reblog        bool `json:"reblog,omitempty"`
	Follow        bool `json:"follow,omitempty"`
	FollowRequest bool `json:"follow_request,omitempty"`
	Favourite     bool `json:"favourite,omitempty"`
	Poll          bool `json:"poll,omitempty"`
	Update        bool `json:"update,omitempty"`
	AdminSignUp   bool `json:"admin.sign_up,omitempty"`
	AdminReport   bool `json:"admin.report,omitempty"`
}
