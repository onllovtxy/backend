package handlers

import (
	"log"
	"net/http"
	"sync"
	"time"

	"github.com/gin-gonic/gin"
	"github.com/gorilla/websocket"
)

const (
	writeWait      = 10 * time.Second
	pongWait       = 60 * time.Second
	pingPeriod     = 30 * time.Second
	maxMessageSize = 4096
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
	writeMu  sync.Mutex
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

	// 心跳保活：服务端周期 Ping，客户端需回 Pong
	go client.pingLoop()

	// 设置读超时与 Pong 处理器
	conn.SetReadLimit(maxMessageSize)
	conn.SetReadDeadline(time.Now().Add(pongWait))
	conn.SetPongHandler(func(string) error {
		return conn.SetReadDeadline(time.Now().Add(pongWait))
	})

	defer func() {
		removeClient(client)
		log.Printf("User %d disconnected from couple room %d WS", userID, coupleID)
	}()

	for {
		_, _, err := conn.ReadMessage()
		if err != nil {
			break
		}
	}
}

func (cl *Client) pingLoop() {
	ticker := time.NewTicker(pingPeriod)
	defer ticker.Stop()

	for range ticker.C {
		cl.writeMu.Lock()
		err := cl.Conn.WriteControl(websocket.PingMessage, []byte{}, time.Now().Add(writeWait))
		cl.writeMu.Unlock()
		if err != nil {
			removeClient(cl)
			cl.Conn.Close()
			return
		}
	}
}

func (cl *Client) sendJSON(msg interface{}) error {
	cl.writeMu.Lock()
	defer cl.writeMu.Unlock()

	cl.Conn.SetWriteDeadline(time.Now().Add(writeWait))
	if err := cl.Conn.WriteJSON(msg); err != nil {
		removeClient(cl)
		cl.Conn.Close()
		return err
	}
	return nil
}

func removeClient(client *Client) {
	hubMutex.Lock()
	defer hubMutex.Unlock()

	clients := coupleHub[client.CoupleID]
	for i, cl := range clients {
		if cl == client {
			coupleHub[client.CoupleID] = append(clients[:i], clients[i+1:]...)
			break
		}
	}
	if len(coupleHub[client.CoupleID]) == 0 {
		delete(coupleHub, client.CoupleID)
	}
}

func BroadcastToCouple(coupleID uint, eventType string, payload interface{}) {
	hubMutex.RLock()
	clients := make([]*Client, 0, len(coupleHub[coupleID]))
	clients = append(clients, coupleHub[coupleID]...)
	hubMutex.RUnlock()

	if len(clients) == 0 {
		return
	}

	msg := gin.H{
		"event":   eventType,
		"payload": payload,
	}

	for _, client := range clients {
		if err := client.sendJSON(msg); err != nil {
			log.Printf("Failed to send WS message to user %d: %v", client.UserID, err)
		}
	}
}
