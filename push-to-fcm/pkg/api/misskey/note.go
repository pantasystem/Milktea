package misskey

import "time"

type NoteDTO struct {
	ID                 string                  `json:"id"`
	CreatedAt          time.Time               `json:"createdAt"`
	Text               *string                 `json:"text,omitempty"`
	CW                 *string                 `json:"cw,omitempty"`
	UserID             string                  `json:"userId"`
	ReplyID            *string                 `json:"replyId,omitempty"`
	RenoteID           *string                 `json:"renoteId,omitempty"`
	ViaMobile          *bool                   `json:"viaMobile,omitempty"`
	Visibility         *NoteVisibilityType     `json:"visibility,omitempty"`
	LocalOnly          *bool                   `json:"localOnly,omitempty"`
	VisibleUserIDs     []string                `json:"visibleUserIds,omitempty"`
	RawReactionEmojis  *EmojisType             `json:"reactionEmojis,omitempty"`
	URL                *string                 `json:"url,omitempty"`
	URI                *string                 `json:"uri,omitempty"`
	RenoteCount        int                     `json:"renoteCount"`
	ReactionCounts     map[string]int          `json:"reactions,omitempty"`
	RawEmojis          *EmojisType             `json:"emojis,omitempty"`
	ReplyCount         int                     `json:"repliesCount"`
	User               Account                 `json:"user"`
	Files              []DriveFile             `json:"files,omitempty"`
	FileIds            []string                `json:"fileIds,omitempty"`
	Poll               *PollDTO                `json:"poll,omitempty"`
	Renote             *NoteDTO                `json:"renote,omitempty"`
	Reply              *NoteDTO                `json:"reply,omitempty"`
	MyReaction         *string                 `json:"myReaction,omitempty"`
	TmpFeaturedID      *string                 `json:"_featuredId_,omitempty"`
	PromotionID        *string                 `json:"_prId_,omitempty"`
	ChannelID          *string                 `json:"channelId,omitempty"`
	Channel            *ChannelInfo            `json:"channel,omitempty"`
	ReactionAcceptance *ReactionAcceptanceType `json:"reactionAcceptance,omitempty"`
}

type ChannelInfo struct {
	ID   string `json:"id"`
	Name string `json:"name"`
}

type NoteVisibilityType string

const (
	Public    NoteVisibilityType = "public"
	Home      NoteVisibilityType = "home"
	Followers NoteVisibilityType = "followers"
	Specified NoteVisibilityType = "specified"
)

type ReactionAcceptanceType int

const (
	LikeOnly ReactionAcceptanceType = iota
	LikeOnly4Remote
)

type EmojisType struct {
	None     bool              `json:"none"`
	Type     string            `json:"type,omitempty"`
	Emojis   []Emoji           `json:"emojis,omitempty"`
	Reaction map[string]string `json:"reaction,omitempty"`
}

type Emoji struct {
	Name string `json:"name"`
	URL  string `json:"url,omitempty"`
	URI  string `json:"uri,omitempty"`
}

type PollDTO struct {
	Choices   []ChoiceDTO `json:"choices"`
	ExpiresAt *time.Time  `json:"expiresAt,omitempty"`
	Multiple  bool        `json:"multiple"`
}

type ChoiceDTO struct {
	Text    string `json:"text"`
	Votes   int    `json:"votes"`
	IsVoted bool   `json:"isVoted"`
}
