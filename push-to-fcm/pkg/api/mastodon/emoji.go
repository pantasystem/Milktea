package mastodon

type TootEmojiDTO struct {
	ShortCode string `json:"shortcode"`
	URL       string `json:"url"`
	StaticURL string `json:"static_url"`
	Category  string `json:"category"`
	VisibleIn bool   `json:"visible_in_picker"`
}
