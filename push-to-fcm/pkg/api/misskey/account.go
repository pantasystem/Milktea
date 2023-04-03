package misskey

import (
	"context"
	"encoding/json"
	"io"
	"net/http"
	"strings"
)

type AccountClient struct {
	BaseUrl string
	Token   string
}

func (r *AccountClient) FindSelf(ctx context.Context) (*Account, error) {

	rb := FindSelfRequest{
		I: r.Token,
	}
	reqJson, err := json.Marshal(rb)
	if err != nil {
		return nil, err
	}
	c := &http.Client{}
	req, err := http.NewRequest("POST", r.BaseUrl+"/api/i", strings.NewReader(string(reqJson)))
	if err != nil {
		return nil, err
	}
	req.Header.Add("Content-Type", "application/json")

	res, err := c.Do(req)
	if err != nil {
		return nil, err
	}
	defer res.Body.Close()

	body, err := io.ReadAll(res.Body)
	if err != nil {
		return nil, err
	}
	var ac Account
	// bodyをAccountに変換
	err = json.Unmarshal(body, &ac)
	if err != nil {
		return nil, err
	}
	return &ac, nil
}

type Account struct {
	ID       string  `json:"id"`
	UserName string  `json:"username"`
	Name     *string `json:"name"`
}

type FindSelfRequest struct {
	I string `json:"i"`
}
