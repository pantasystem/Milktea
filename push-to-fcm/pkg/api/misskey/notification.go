package misskey

import "time"

type NotificationDTO struct {
	ID        string    `json:"id"`
	CreatedAt time.Time `json:"createdAt"`
	Type      string    `json:"type"`
	UserID    string    `json:"userId,omitempty"`
	User      Account   `json:"user,omitempty"`
	Note      NoteDTO   `json:"note,omitempty"`
	NoteID    string    `json:"noteId,omitempty"`
	Reaction  string    `json:"reaction,omitempty"`
	IsRead    bool      `json:"isRead,omitempty"`
	Choice    int       `json:"choice,omitempty"`
}
