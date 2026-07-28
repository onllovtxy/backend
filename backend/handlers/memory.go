package handlers

import (
	"loveever-backend/database"
	"loveever-backend/models"
	"net/http"
	"strconv"
	"time"

	"github.com/gin-gonic/gin"
)

func GetMemories(c *gin.Context) {
	coupleID := c.MustGet("couple_id").(uint)

	var list []models.Memory
	database.DB.Where("couple_id = ?", coupleID).Order("memory_date desc").Find(&list)

	c.JSON(http.StatusOK, gin.H{"data": list})
}

func CreateMemory(c *gin.Context) {
	coupleID := c.MustGet("couple_id").(uint)
	userID := c.MustGet("user_id").(uint)

	var req struct {
		Title      string `json:"title" binding:"required"`
		Content    string `json:"content" binding:"required"`
		MemoryDate string `json:"memory_date" binding:"required"`
		ImageURL   string `json:"image_url"`
	}

	if err := c.ShouldBindJSON(&req); err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": err.Error()})
		return
	}

	mem := models.Memory{
		CoupleID:   coupleID,
		Title:      req.Title,
		Content:    req.Content,
		MemoryDate: req.MemoryDate,
		ImageURL:   req.ImageURL,
		CreatedBy:  userID,
		CreatedAt:  time.Now(),
	}

	database.DB.Create(&mem)

	BroadcastToCouple(coupleID, "memory_added", mem)

	c.JSON(http.StatusOK, gin.H{"data": mem})
}

func DeleteMemory(c *gin.Context) {
	coupleID := c.MustGet("couple_id").(uint)
	idParam := c.Param("id")
	id, _ := strconv.Atoi(idParam)

	database.DB.Where("id = ? AND couple_id = ?", id, coupleID).Delete(&models.Memory{})

	BroadcastToCouple(coupleID, "memory_deleted", gin.H{"id": id})

	c.JSON(http.StatusOK, gin.H{"message": "Memory deleted"})
}
