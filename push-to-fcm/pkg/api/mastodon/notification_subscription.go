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
	var ps PushSubscription
	if err := r.doRequest(ctx, "POST", "/api/v1/push/subscription", req, &ps); err != nil {
		return nil, err
	}
	return &ps, nil
}

type PushSubscription struct {
	ID        string `json:"id"`
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
	Subscrption struct {
		Endpoint string `json:"endpoint"`
		Keys     struct {
			P256dh string `json:"p256dh"`
			Auth   string `json:"auth"`
		} `json:"keys"`
	} `json:"subscription"`

	Data struct {
		Alerts struct {
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
		Policy string `json:"policy,omitempty"`
	} `json:"data,omitempty"`
}

func (r *NotificationSubscriptionClient) doRequest(ctx context.Context, method, path string, payload interface{}, out interface{}) error {
	url := r.BaseUrl + path

	req, err := http.NewRequestWithContext(ctx, method, url, nil)
	if err != nil {
		return err
	}

	if payload != nil {
		req.Header.Set("Content-Type", "application/json")
		buf := bytes.NewBuffer(nil)
		if err := json.NewEncoder(buf).Encode(payload); err != nil {
			return err
		}
		req.Body = io.NopCloser(buf)
	}

	if r.Token != "" {
		req.Header.Set("Authorization", "Bearer "+r.Token)
	}

	resp, err := http.DefaultClient.Do(req)
	if err != nil {
		return err
	}
	defer resp.Body.Close()

	if resp.StatusCode >= 400 {
		return errors.New(resp.Status)
	}

	if out != nil {
		if err := json.NewDecoder(resp.Body).Decode(out); err != nil {
			return err
		}
	}

	return nil
}
