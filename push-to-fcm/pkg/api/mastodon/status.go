package mastodon

import "time"

type TootStatusDTO struct {
	ID               string                `json:"id"`
	URI              string                `json:"uri"`
	CreatedAt        time.Time             `json:"created_at"`
	Account          Account               `json:"account"`
	Content          *string               `json:"content,omitempty"`
	Sensitive        bool                  `json:"sensitive"`
	SpoilerText      string                `json:"spoiler_text"`
	MediaAttachments []TootMediaAttachment `json:"media_attachments"`
	Emojis           []TootEmojiDTO        `json:"emojis"`
	ReblogsCount     int                   `json:"reblogs_count"`
	FavouritesCount  *int                  `json:"favourites_count,omitempty"`
	RepliesCount     int                   `json:"replies_count"`
	URL              *string               `json:"url,omitempty"`
	InReplyToID      *string               `json:"in_reply_to_id,omitempty"`
	InReplyAccountID *string               `json:"in_reply_account_id,omitempty"`
	Reblog           *TootStatusDTO        `json:"reblog,omitempty"`
	Poll             *TootPollDTO          `json:"poll,omitempty"`
	Language         *string               `json:"language,omitempty"`
	Text             *string               `json:"text,omitempty"`
	EditedAt         *time.Time            `json:"edited_at,omitempty"`
	Favourited       *bool                 `json:"favourited,omitempty"`
	Reblogged        *bool                 `json:"reblogged,omitempty"`
	Muted            *bool                 `json:"muted,omitempty"`
	Bookmarked       *bool                 `json:"bookmarked,omitempty"`
	Pinned           *bool                 `json:"pinned,omitempty"`
	EmojiReactions   []EmojiReactionCount  `json:"emoji_reactions,omitempty"`
	Quote            *TootStatusDTO        `json:"quote,omitempty"`
	CircleID         *string               `json:"circle_id,omitempty"`
	VisibilityEx     *string               `json:"visibility_ex,omitempty"`
}

type EmojiReactionCount struct {
	Name       string   `json:"name"`
	Count      int      `json:"count"`
	AccountIds []string `json:"account_ids"`
	Me         *bool    `json:"me,omitempty"`
	URL        *string  `json:"url,omitempty"`
	Domain     *string  `json:"domain,omitempty"`
	StaticURL  *string  `json:"static_url,omitempty"`
}

type TootPollDTO struct {
	ID          string    `json:"id"`
	ExpiresAt   time.Time `json:"expires_at,omitempty"`
	Expired     bool      `json:"expired,omitempty"`
	Multiple    bool      `json:"multiple,omitempty"`
	VotesCount  int       `json:"votes_count,omitempty"`
	VotersCount int       `json:"voters_count,omitempty"`
	Options     []Option  `json:"options"`
	Emojis      []struct {
		ShortCode string `json:"shortcode"`
		URL       string `json:"url"`
		StaticURL string `json:"static_url"`
	} `json:"emojis"`
	Voted    bool  `json:"voted,omitempty"`
	OwnVotes []int `json:"own_votes,omitempty"`
}

type Option struct {
	Title      string `json:"title"`
	VotesCount int    `json:"votes_count,omitempty"`
}

type TootMediaAttachment struct {
	ID          string `json:"id"`
	Type        string `json:"type"`
	URL         string `json:"url,omitempty"`
	PreviewURL  string `json:"preview_url,omitempty"`
	RemoteURL   string `json:"remote_url,omitempty"`
	Description string `json:"description,omitempty"`
	Blurhash    string `json:"blurhash,omitempty"`
}
