package handlers

import (
	"log"
	"net/http"
	"sync"

	"github.com/gin-gonic/gin"
	"github.com/gorilla/websocket"
)

var upgrader = websocket.Upgrader{
	CheckOrigin: func(r *http.Request) bool {
		return true
	},
}

type Client struct {
	Conn     *websocket.Conn
	CoupleID uint
	UserID   uint
}

var (
	hubMutex sync.RWMutex
	// couple_id -> slice of Client pointers
	coupleHub = make(map[uint][]*Client)
)

func HandleWebSocket(c *gin.Context) {
	coupleIDVal, exists := c.Get("couple_id")
	if !exists || coupleIDVal.(uint) == 0 {
		c.JSON(http.StatusBadRequest, gin.H{"error": "No couple associated"})
		return
	}
	coupleID := coupleIDVal.(uint)
	userID := c.MustGet("user_id").(uint)

	conn, err := upgrader.Upgrade(c.Writer, c.Request, nil)
	if err != nil {
		log.Printf("Failed to upgrade WebSocket: %v", err)
		return
	}

	client := &Client{
		Conn:     conn,
		CoupleID: coupleID,
		UserID:   userID,
	}

	hubMutex.Lock()
	coupleHub[coupleID] = append(coupleHub[coupleID], client)
	hubMutex.Unlock()

	log.Printf("User %d connected to couple room %d WS", userID, coupleID)

	// Keep connection alive & listen for disconnect
	defer func() {
		conn.Close()
		hubMutex.Lock()
		clients := coupleHub[coupleID]
		for i, cl := range clients {
			if cl == client {
				coupleHub[coupleID] = append(clients[:i], clients[i+1:]...)
				break
			}
		}
		hubMutex.Unlock()
		log.Printf("User %d disconnected from couple room %d WS", userID, coupleID)
	}()

	for {
		_, _, err := conn.ReadMessage()
		if err != nil {
			break
		}
	}
}

func BroadcastToCouple(coupleID uint, eventType string, payload interface{}) {
	hubMutex.RLock()
	defer hubMutex.RUnlock()

	clients, ok := coupleHub[coupleID]
	if !ok || len(clients) == 0 {
		return
	}

	msg := gin.H{
		"event":   eventType,
		"payload": payload,
	}

	for _, client := range clients {
		err := client.Conn.WriteJSON(msg)
		if err != nil {
			log.Printf("Failed to send WS message to user %d: %v", client.UserID, err)
		}
	}
}
