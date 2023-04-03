package mastodon

import (
	"time"
)

type MstNotificationDTO struct {
	ID            string           `json:"id"`
	Type          NotificationType `json:"type"`
	CreatedAt     time.Time        `json:"created_at"`
	Account       Account          `json:"account"`
	Status        *TootStatusDTO   `json:"status,omitempty"`
	Report        *MstReportDTO    `json:"report,omitempty"`
	EmojiReaction *EmojiReaction   `json:"emoji_reaction,omitempty"`
}

type NotificationType string

const (
	Mention         NotificationType = "mention"
	Status          NotificationType = "status"
	Reblog          NotificationType = "reblog"
	Follow          NotificationType = "follow"
	FollowRequest   NotificationType = "follow_request"
	Favourite       NotificationType = "favourite"
	Poll            NotificationType = "poll"
	Update          NotificationType = "update"
	AdminSignUp     NotificationType = "admin.sign_up"
	AdminReport     NotificationType = "admin.report"
	EmojiReactionNT NotificationType = "emoji_reaction"
)

type EmojiReaction struct {
	Name      string  `json:"name"`
	Count     int     `json:"count"`
	Me        *bool   `json:"me,omitempty"`
	URL       *string `json:"url,omitempty"`
	Domain    *string `json:"domain,omitempty"`
	StaticURL *string `json:"static_url,omitempty"`
}
