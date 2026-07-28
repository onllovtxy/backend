package handlers

import (
	"loveever-backend/database"
	"loveever-backend/models"
	"net/http"
	"strconv"
	"time"

	"github.com/gin-gonic/gin"
)

func GetAnniversaries(c *gin.Context) {
	coupleID := c.MustGet("couple_id").(uint)

	var list []models.Anniversary
	database.DB.Where("couple_id = ?", coupleID).Order("is_pinned desc, target_date asc").Find(&list)

	c.JSON(http.StatusOK, gin.H{"data": list})
}

func CreateAnniversary(c *gin.Context) {
	coupleID := c.MustGet("couple_id").(uint)
	userID := c.MustGet("user_id").(uint)

	var req struct {
		Title      string `json:"title" binding:"required"`
		TargetDate string `json:"target_date" binding:"required"`
		IsPinned   bool   `json:"is_pinned"`
		Icon       string `json:"icon"`
	}

	if err := c.ShouldBindJSON(&req); err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": err.Error()})
		return
	}

	icon := req.Icon
	if icon == "" {
		icon = "heart"
	}

	anniv := models.Anniversary{
		CoupleID:   coupleID,
		Title:      req.Title,
		TargetDate: req.TargetDate,
		IsPinned:   req.IsPinned,
		Icon:       icon,
		CreatedBy:  userID,
		CreatedAt:  time.Now(),
	}

	database.DB.Create(&anniv)

	// 通过 WebSocket 向同房间的伴侣推送更新通知
	BroadcastToCouple(coupleID, "anniversary_added", anniv)

	c.JSON(http.StatusOK, gin.H{"data": anniv})
}

func TogglePinAnniversary(c *gin.Context) {
	coupleID := c.MustGet("couple_id").(uint)
	idParam := c.Param("id")
	id, _ := strconv.Atoi(idParam)

	var anniv models.Anniversary
	if err := database.DB.Where("id = ? AND couple_id = ?", id, coupleID).First(&anniv).Error; err != nil {
		c.JSON(http.StatusNotFound, gin.H{"error": "Not found"})
		return
	}

	anniv.IsPinned = !anniv.IsPinned
	database.DB.Save(&anniv)

	BroadcastToCouple(coupleID, "anniversary_updated", anniv)

	c.JSON(http.StatusOK, gin.H{"data": anniv})
}

func DeleteAnniversary(c *gin.Context) {
	coupleID := c.MustGet("couple_id").(uint)
	idParam := c.Param("id")
	id, _ := strconv.Atoi(idParam)

	database.DB.Where("id = ? AND couple_id = ?", id, coupleID).Delete(&models.Anniversary{})

	BroadcastToCouple(coupleID, "anniversary_deleted", gin.H{"id": id})

	c.JSON(http.StatusOK, gin.H{"message": "Deleted successfully"})
}

func UpdatePairDate(c *gin.Context) {
	coupleID := c.MustGet("couple_id").(uint)
	var req struct {
		PairDate string `json:"pair_date" binding:"required"`
	}
	if err := c.ShouldBindJSON(&req); err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": err.Error()})
		return
	}

	database.DB.Model(&models.Couple{}).Where("id = ?", coupleID).Update("pair_date", req.PairDate)

	BroadcastToCouple(coupleID, "pair_date_updated", gin.H{"pair_date": req.PairDate})

	c.JSON(http.StatusOK, gin.H{"message": "Pair date updated", "pair_date": req.PairDate})
}
