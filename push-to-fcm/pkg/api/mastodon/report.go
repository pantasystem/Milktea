package mastodon

import "time"

type MstReportDTO struct {
	ID            string     `json:"id"`
	ActionTaken   *bool      `json:"action_taken,omitempty"`
	ActionTakenAt *time.Time `json:"action_taken_at,omitempty"`
	Category      *string    `json:"category,omitempty"`
	Comment       *string    `json:"comment,omitempty"`
	Forwarded     *bool      `json:"forwarded,omitempty"`
	CreatedAt     *time.Time `json:"created_at,omitempty"`
	StatusIDs     []string   `json:"status_ids,omitempty"`
	RuleIDs       []string   `json:"rule_ids,omitempty"`
	TargetAccount *Account   `json:"target_account,omitempty"`
}
