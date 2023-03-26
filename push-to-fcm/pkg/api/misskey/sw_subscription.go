package misskey

import "context"

type State string

const (
	StateAlreadySubscribed State = "already-subscribed"
	StateSubscribed        State = "subscribed"
)

type SWSubscription struct {
	BaseUrl string
	Token   string
}

type SubscribeReqest struct {
	Endpoint  string `json:"endpoint"`
	Auth      string `json:"auth"`
	PublicKey string `json:"publicKey"`
}

type InternalSubscribeRequest struct {
	I         string `json:"i"`
	Endpoint  string `json:"endpoint"`
	Auth      string `json:"auth"`
	PublicKey string `json:"publicKey"`
}

type SubscribeResponse struct {
	State State   `json:"state"`
	Key   *string `json:"key"`
}

func (r *SWSubscription) Subscribe(ctx context.Context, req SubscribeReqest) (*SubscribeResponse, error) {
	// TODO
	return nil, nil
}

func (r *SWSubscription) Unsubscribe(ctx context.Context, endpoint string) error {
	// TODO
	return nil
}
