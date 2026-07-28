package models

import (
	"time"
)

type User struct {
	ID          uint      `gorm:"primaryKey" json:"id"`
	Username    string    `gorm:"uniqueIndex;not null" json:"username"`
	Password    string    `json:"-"`
	DisplayName string    `json:"display_name"`
	AvatarURL   string    `json:"avatar_url"`
	CoupleID    *uint     `json:"couple_id"`
	Role        string    `json:"role"` // "partner_a" or "partner_b"
	CreatedAt   time.Time `json:"created_at"`
}

type Couple struct {
	ID         uint      `gorm:"primaryKey" json:"id"`
	InviteCode string    `gorm:"uniqueIndex;not null" json:"invite_code"`
	PairDate   string    `json:"pair_date"` // YYYY-MM-DD
	CreatedAt  time.Time `json:"created_at"`
}

type Anniversary struct {
	ID         uint      `gorm:"primaryKey" json:"id"`
	CoupleID   uint      `gorm:"index;not null" json:"couple_id"`
	Title      string    `json:"title"`
	TargetDate string    `json:"target_date"` // YYYY-MM-DD
	IsPinned   bool      `json:"is_pinned"`
	Icon       string    `json:"icon"`
	CreatedBy  uint      `json:"created_by"`
	CreatedAt  time.Time `json:"created_at"`
}

type Memory struct {
	ID         uint      `gorm:"primaryKey" json:"id"`
	CoupleID   uint      `gorm:"index;not null" json:"couple_id"`
	Title      string    `json:"title"`
	Content    string    `json:"content"`
	MemoryDate string    `json:"memory_date"` // YYYY-MM-DD
	ImageURL   string    `json:"image_url"`
	CreatedBy  uint      `json:"created_by"`
	CreatedAt  time.Time `json:"created_at"`
}

// DTOs
type RegisterReq struct {
	Username    string `json:"username" binding:"required"`
	Password    string `json:"password" binding:"required"`
	DisplayName string `json:"display_name" binding:"required"`
	PairDate    string `json:"pair_date"`
}

type LoginReq struct {
	Username string `json:"username" binding:"required"`
	Password string `json:"password" binding:"required"`
}

type PairReq struct {
	InviteCode string `json:"invite_code" binding:"required"`
}
