package main

import (
	"log"
	"loveever-backend/database"
	"loveever-backend/handlers"

	"github.com/gin-gonic/gin"
)

func main() {
	log.Println("Starting LoveEver Go High-Performance Backend Server...")

	// 初始化数据库
	database.InitDB()

	r := gin.Default()

	// CORS 中间件
	r.Use(func(c *gin.Context) {
		c.Writer.Header().Set("Access-Control-Allow-Origin", "*")
		c.Writer.Header().Set("Access-Control-Allow-Credentials", "true")
		c.Writer.Header().Set("Access-Control-Allow-Headers", "Content-Type, Content-Length, Accept-Encoding, X-CSRF-Token, Authorization, accept, origin, Cache-Control, X-Requested-With")
		c.Writer.Header().Set("Access-Control-Allow-Methods", "POST, OPTIONS, GET, PUT, DELETE")

		if c.Request.Method == "OPTIONS" {
			c.AbortWithStatus(204)
			return
		}
		c.Next()
	})

	api := r.Group("/api/v1")
	{
		// 公开路由
		api.POST("/auth/register", handlers.Register)
		api.POST("/auth/login", handlers.Login)

		// 需 Token 鉴权路由
		auth := api.Group("")
		auth.Use(handlers.AuthMiddleware())
		{
			auth.GET("/profile", handlers.GetProfile)
			auth.POST("/auth/pair", handlers.PairCouple)

			// 纪念日
			auth.GET("/anniversaries", handlers.GetAnniversaries)
			auth.POST("/anniversaries", handlers.CreateAnniversary)
			auth.PUT("/anniversaries/:id/pin", handlers.TogglePinAnniversary)
			auth.DELETE("/anniversaries/:id", handlers.DeleteAnniversary)
			auth.PUT("/couple/pair-date", handlers.UpdatePairDate)

			// 时光回忆
			auth.GET("/memories", handlers.GetMemories)
			auth.POST("/memories", handlers.CreateMemory)
			auth.DELETE("/memories/:id", handlers.DeleteMemory)

			// WebSocket 实时连接
			auth.GET("/ws", handlers.HandleWebSocket)
		}
	}

	log.Println("Server running on http://0.0.0.0:8080")
	if err := r.Run(":8080"); err != nil {
		log.Fatalf("Server failed to run: %v", err)
	}
}
