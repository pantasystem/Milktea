package misskey

import "time"

type DriveFile struct {
	ID            string    `json:"id"`
	CreatedAt     time.Time `json:"createdAt"`
	Name          string    `json:"name"`
	Type          string    `json:"type"`
	MD5           string    `json:"md5"`
	Size          *int      `json:"size,omitempty"`
	UserID        *string   `json:"userId,omitempty"`
	FolderID      *string   `json:"folderId,omitempty"`
	Comment       *string   `json:"comment,omitempty"`
	IsSensitive   *bool     `json:"isSensitive,omitempty"`
	URL           string    `json:"url"`
	ThumbnailURL  *string   `json:"thumbnailUrl,omitempty"`
	AttachedNotes []string  `json:"attachedNoteIds,omitempty"`
	Blurhash      *string   `json:"blurhash,omitempty"`
}
