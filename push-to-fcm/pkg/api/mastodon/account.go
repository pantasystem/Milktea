package mastodon

import (
	"context"
	"encoding/json"
	"fmt"
	"net/http"
	"time"
)

type AccountClient struct {
	BaseUrl string
	Token   string
}

func (r *AccountClient) VerifyCredentials(ctx context.Context) (*Account, error) {
	reqURL := fmt.Sprintf("%s/api/v1/accounts/verify_credentials", r.BaseUrl)
	req, err := http.NewRequest("GET", reqURL, nil)
	if err != nil {
		return nil, err
	}
	req.Header.Set("Authorization", "Bearer "+r.Token)

	// Send HTTP request
	client := http.DefaultClient
	resp, err := client.Do(req)
	if err != nil {
		return nil, err
	}
	defer resp.Body.Close()

	// Decode JSON response
	var account Account
	if err := json.NewDecoder(resp.Body).Decode(&account); err != nil {
		return nil, err
	}

	return &account, nil
}

type Account struct {
	ID             string         `json:"id"`
	Username       string         `json:"username"`
	Acct           string         `json:"acct"`
	DisplayName    string         `json:"display_name"`
	Locked         bool           `json:"locked"`
	Bot            bool           `json:"bot"`
	CreatedAt      time.Time      `json:"created_at"`
	Note           string         `json:"note"`
	URL            string         `json:"url"`
	Avatar         string         `json:"avatar"`
	AvatarStatic   string         `json:"avatar_static"`
	Header         string         `json:"header"`
	HeaderStatic   string         `json:"header_static"`
	Emojis         []TootEmojiDTO `json:"emojis"`
	FollowersCount int64          `json:"followers_count"`
	FollowingCount int64          `json:"following_count"`
	StatusesCount  int64          `json:"statuses_count"`
}

type TootEmojiDTO struct {
	ShortCode string   `json:"shortcode"`
	URL       string   `json:"url"`
	StaticURL string   `json:"static_url"`
	Category  string   `json:"category"`
	VisibleIn []string `json:"visible_in_picker"`
}
