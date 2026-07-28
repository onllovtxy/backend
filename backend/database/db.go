package database

import (
	"log"
	"loveever-backend/models"
	"os"
	"path/filepath"
	"time"

	"github.com/glebarez/sqlite"
	"gorm.io/gorm"
)

var DB *gorm.DB

func InitDB() {
	// 确保自托管 data 目录存在
	dbDir := "data"
	if _, err := os.Stat(dbDir); os.IsNotExist(err) {
		os.MkdirAll(dbDir, 0755)
	}

	dbPath := filepath.Join(dbDir, "loveever.db")

	var err error
	DB, err = gorm.Open(sqlite.Open(dbPath), &gorm.Config{})
	if err != nil {
		log.Fatalf("Failed to connect database: %v", err)
	}

	// 自动数据迁移
	err = DB.AutoMigrate(&models.User{}, &models.Couple{}, &models.Anniversary{}, &models.Memory{})
	if err != nil {
		log.Fatalf("Failed to migrate database: %v", err)
	}

	seedInitialData()
}

func seedInitialData() {
	var count int64
	DB.Model(&models.Couple{}).Count(&count)
	if count > 0 {
		return
	}

	// 创建 Demo 默认情侣与纪念日
	couple := models.Couple{
		InviteCode: "LOVE-520",
		PairDate:   "2023-05-20",
		CreatedAt:  time.Now(),
	}
	DB.Create(&couple)

	userA := models.User{
		Username:    "user1",
		Password:    "$2a$10$wN3tV4U8v1Y1.X5M8qF1.e1qX1qX1qX1qX1qX1qX1qX1qX1qX1qX",
		DisplayName: "宝贝",
		AvatarURL:   "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=150",
		CoupleID:    &couple.ID,
		Role:        "partner_a",
	}
	DB.Create(&userA)

	userB := models.User{
		Username:    "user2",
		Password:    "$2a$10$wN3tV4U8v1Y1.X5M8qF1.e1qX1qX1qX1qX1qX1qX1qX1qX1qX1qX",
		DisplayName: "亲爱的",
		AvatarURL:   "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=150",
		CoupleID:    &couple.ID,
		Role:        "partner_b",
	}
	DB.Create(&userB)

	// 初始纪念日
	anniversaries := []models.Anniversary{
		{CoupleID: couple.ID, Title: "相爱在一起", TargetDate: "2023-05-20", IsPinned: true, Icon: "heart", CreatedBy: userA.ID},
		{CoupleID: couple.ID, Title: "相识 1000 天纪念", TargetDate: "2026-02-14", IsPinned: true, Icon: "star", CreatedBy: userA.ID},
		{CoupleID: couple.ID, Title: "宝贝的生日", TargetDate: "2026-10-24", IsPinned: false, Icon: "cake", CreatedBy: userA.ID},
	}
	for _, a := range anniversaries {
		DB.Create(&a)
	}

	// 初始回忆相册
	memories := []models.Memory{
		{
			CoupleID:   couple.ID,
			Title:      "第一次看海 🌊",
			Content:    "青岛的海风真的很温柔，踩在沙滩上夕阳把我们的影子拉得很长很长。",
			MemoryDate: "2023-08-15",
			ImageURL:   "https://images.unsplash.com/photo-1507525428034-b723cf961d3e?w=600",
			CreatedBy:  userA.ID,
		},
	}
	for _, m := range memories {
		DB.Create(&m)
	}

	log.Println("DB initial seed complete!")
}
