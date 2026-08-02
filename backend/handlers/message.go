package handlers

import (
	"fmt"
	"loveever-backend/database"
	"loveever-backend/models"
	"net/http"
	"os"
	"path/filepath"
	"strconv"
	"strings"
	"time"

	"github.com/gin-gonic/gin"
)

const maxUploadSize = 20 << 20 // 20MB

var allowedExts = map[string]bool{
	".jpg": true, ".jpeg": true, ".png": true, ".webp": true, ".gif": true,
	".m4a": true, ".aac": true, ".amr": true, ".wav": true, ".mp3": true,
}

func GetMessages(c *gin.Context) {
	coupleID := c.MustGet("couple_id").(uint)

	beforeID, _ := strconv.Atoi(c.DefaultQuery("before_id", "0"))
	limit := 50
	if l, err := strconv.Atoi(c.DefaultQuery("limit", "50")); err == nil && l > 0 && l <= 100 {
		limit = l
	}

	var list []models.Message
	q := database.DB.Where("couple_id = ?", coupleID)
	if beforeID > 0 {
		q = q.Where("id < ?", beforeID)
	}
	q.Order("id desc").Limit(limit).Find(&list)

	// 倒序拉取后转为升序返回
	for i, j := 0, len(list)-1; i < j; i, j = i+1, j-1 {
		list[i], list[j] = list[j], list[i]
	}

	c.JSON(http.StatusOK, gin.H{"data": list})
}

func SendMessage(c *gin.Context) {
	coupleID := c.MustGet("couple_id").(uint)
	userID := c.MustGet("user_id").(uint)

	var req models.SendMessageReq
	if err := c.ShouldBindJSON(&req); err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": err.Error()})
		return
	}

	if req.Type != "text" && req.Type != "image" && req.Type != "voice" {
		c.JSON(http.StatusBadRequest, gin.H{"error": "invalid message type"})
		return
	}

	msg := models.Message{
		CoupleID:  coupleID,
		SenderID:  userID,
		Type:      req.Type,
		Content:   req.Content,
		Duration:  req.Duration,
		CreatedAt: time.Now(),
	}
	database.DB.Create(&msg)

	BroadcastToCouple(coupleID, "message_new", msg)

	c.JSON(http.StatusOK, gin.H{"data": msg})
}

func UploadFile(c *gin.Context) {
	file, err := c.FormFile("file")
	if err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": "file field required"})
		return
	}

	if file.Size > maxUploadSize {
		c.JSON(http.StatusBadRequest, gin.H{"error": "file too large, max 20MB"})
		return
	}

	ext := strings.ToLower(filepath.Ext(file.Filename))
	if !allowedExts[ext] {
		c.JSON(http.StatusBadRequest, gin.H{"error": "unsupported file type"})
		return
	}

	if err := os.MkdirAll("uploads", 0755); err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"error": "failed to create uploads dir"})
		return
	}

	name := fmt.Sprintf("msg_%d_%d%s", time.Now().UnixNano(), time.Now().Unix()%1000, ext)
	dst := filepath.Join("uploads", name)
	if err := c.SaveUploadedFile(file, dst); err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"error": "failed to save file"})
		return
	}

	c.JSON(http.StatusOK, gin.H{"url": "/api/v1/files/" + name})
}

// ServeFile 提供鉴权文件访问，防止目录穿越
func ServeFile(c *gin.Context) {
	name := c.Param("name")
	if name == "" || strings.ContainsAny(name, "/\\") || strings.Contains(name, "..") {
		c.JSON(http.StatusBadRequest, gin.H{"error": "invalid filename"})
		return
	}

	path := filepath.Join("uploads", name)
	if _, err := os.Stat(path); err != nil {
		c.JSON(http.StatusNotFound, gin.H{"error": "file not found"})
		return
	}

	c.File(path)
}
