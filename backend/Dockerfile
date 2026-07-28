# ==========================================
# 阶段 1: 编译构建 (Build Stage)
# ==========================================
FROM golang:1.22-alpine AS builder

# 安装基本构建工具
RUN apk add --no-gc --no-cache git gcc musl-dev

WORKDIR /app

# 复制依赖配置并下载
COPY go.mod go.sum ./
RUN go mod download

# 复制源码并编译成无依赖静态二进制文件
COPY . .
RUN CGO_ENABLED=0 GOOS=linux GOARCH=amd64 go build -ldflags="-s -w" -o loveever-server main.go

# ==========================================
# 阶段 2: 极简运行镜像 (Production Image)
# ==========================================
FROM alpine:3.19

# 安装时区数据与根证书
RUN apk --no-cache add ca-certificates tzdata && \
    cp /usr/share/zoneinfo/Asia/Shanghai /etc/localtime && \
    echo "Asia/Shanghai" > /etc/timezone

WORKDIR /app

# 从编译阶段复制二进制文件
COPY --from=builder /app/loveever-server .

# 创建持久化数据与上传文件目录
RUN mkdir -p /app/data /app/uploads

# 暴露 8080 端口
EXPOSE 8080

# 环境变量设置
ENV GIN_MODE=release

# 启动命令
CMD ["./loveever-server"]
