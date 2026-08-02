package handlers

import (
	"fmt"
	"loveever-backend/database"
	"loveever-backend/models"
	"net/http"
	"os"
	"time"

	"github.com/gin-gonic/gin"
	"github.com/golang-jwt/jwt/v5"
	"golang.org/x/crypto/bcrypt"
)

func jwtSecret() []byte {
	if s := os.Getenv("LOVEEVER_JWT_SECRET"); s != "" {
		return []byte(s)
	}
	return []byte("loveever-secret-key-2026")
}

func GenerateJWT(userID uint, coupleID *uint) (string, error) {
	var cID uint = 0
	if coupleID != nil {
		cID = *coupleID
	}
	claims := jwt.MapClaims{
		"user_id":   userID,
		"couple_id": cID,
		"exp":       time.Now().Add(time.Hour * 24 * 30).Unix(),
	}
	token := jwt.NewWithClaims(jwt.SigningMethodHS256, claims)
	return token.SignedString(jwtSecret())
}

func AuthMiddleware() gin.HandlerFunc {
	return func(c *gin.Context) {
		tokenString := c.GetHeader("Authorization")
		if len(tokenString) > 7 && tokenString[:7] == "Bearer " {
			tokenString = tokenString[7:]
		}

		if tokenString == "" {
			c.JSON(http.StatusUnauthorized, gin.H{"error": "Missing authorization token"})
			c.Abort()
			return
		}

		token, err := jwt.Parse(tokenString, func(t *jwt.Token) (interface{}, error) {
			return jwtSecret(), nil
		})

		if err != nil || !token.Valid {
			c.JSON(http.StatusUnauthorized, gin.H{"error": "Invalid or expired token"})
			c.Abort()
			return
		}

		claims := token.Claims.(jwt.MapClaims)
		userID := uint(claims["user_id"].(float64))
		coupleID := uint(claims["couple_id"].(float64))

		c.Set("user_id", userID)
		c.Set("couple_id", coupleID)
		c.Next()
	}
}

func Register(c *gin.Context) {
	var req models.RegisterReq
	if err := c.ShouldBindJSON(&req); err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": err.Error()})
		return
	}

	hashedPassword, err := bcrypt.GenerateFromPassword([]byte(req.Password), bcrypt.DefaultCost)
	if err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"error": "Failed to hash password"})
		return
	}

	// 默认自动创建包含专属邀请码的 Couple 空间
	pairDate := req.PairDate
	if pairDate == "" {
		pairDate = time.Now().Format("2006-01-02")
	}
	inviteCode := fmt.Sprintf("LOVE-%04d", time.Now().UnixNano()%10000)

	couple := models.Couple{
		InviteCode: inviteCode,
		PairDate:   pairDate,
		CreatedAt:  time.Now(),
	}
	database.DB.Create(&couple)

	user := models.User{
		Username:    req.Username,
		Password:    string(hashedPassword),
		DisplayName: req.DisplayName,
		AvatarURL:   "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=150",
		CoupleID:    &couple.ID,
		Role:        "partner_a",
		CreatedAt:   time.Now(),
	}

	if err := database.DB.Create(&user).Error; err != nil {
		// 用户名冲突时回滚已创建的 Couple，避免孤儿空间
		database.DB.Delete(&models.Couple{}, couple.ID)
		c.JSON(http.StatusBadRequest, gin.H{"error": "Username already exists"})
		return
	}

	token, _ := GenerateJWT(user.ID, user.CoupleID)

	c.JSON(http.StatusOK, gin.H{
		"token":       token,
		"user":        user,
		"couple":      couple,
		"invite_code": couple.InviteCode,
	})
}

func Login(c *gin.Context) {
	var req models.LoginReq
	if err := c.ShouldBindJSON(&req); err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": err.Error()})
		return
	}

	var user models.User
	if err := database.DB.Where("username = ?", req.Username).First(&user).Error; err != nil {
		c.JSON(http.StatusUnauthorized, gin.H{"error": "Invalid username or password"})
		return
	}

	if err := bcrypt.CompareHashAndPassword([]byte(user.Password), []byte(req.Password)); err != nil {
		c.JSON(http.StatusUnauthorized, gin.H{"error": "Invalid username or password"})
		return
	}

	var couple models.Couple
	if user.CoupleID != nil {
		database.DB.First(&couple, *user.CoupleID)
	}

	token, _ := GenerateJWT(user.ID, user.CoupleID)

	c.JSON(http.StatusOK, gin.H{
		"token":  token,
		"user":   user,
		"couple": couple,
	})
}

func PairCouple(c *gin.Context) {
	var req models.PairReq
	if err := c.ShouldBindJSON(&req); err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": err.Error()})
		return
	}

	userID := c.MustGet("user_id").(uint)

	var targetCouple models.Couple
	if err := database.DB.Where("invite_code = ?", req.InviteCode).First(&targetCouple).Error; err != nil {
		c.JSON(http.StatusNotFound, gin.H{"error": "Invalid invite code"})
		return
	}

	// 将当前用户更新绑定至目标 Couple
	database.DB.Model(&models.User{}).Where("id = ?", userID).Updates(map[string]interface{}{
		"couple_id": targetCouple.ID,
		"role":      "partner_b",
	})

	token, _ := GenerateJWT(userID, &targetCouple.ID)

	c.JSON(http.StatusOK, gin.H{
		"message": "Successfully paired!",
		"couple":  targetCouple,
		"token":   token,
	})
}

func GetProfile(c *gin.Context) {
	userID := c.MustGet("user_id").(uint)
	var user models.User
	database.DB.First(&user, userID)

	var couple models.Couple
	if user.CoupleID != nil {
		database.DB.First(&couple, *user.CoupleID)
	}

	// 查找伴侣 Profile
	var partner models.User
	if user.CoupleID != nil {
		database.DB.Where("couple_id = ? AND id != ?", *user.CoupleID, userID).First(&partner)
	}

	c.JSON(http.StatusOK, gin.H{
		"user":           user,
		"couple":         couple,
		"partner_name":   partner.DisplayName,
		"partner_avatar": partner.AvatarURL,
	})
}
