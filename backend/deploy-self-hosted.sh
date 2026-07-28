#!/bin/bash

# ========================================================
# 恋念 (LoveEver) Go 后端 - 纯自托管一键部署脚本
# ========================================================

echo "🚀 开始进行 LoveEver 自托管后端一键部署..."

# 检查 Docker 是否安装
if ! command -v docker &> /dev/null
then
    echo "❌ 错误: 未检测到 Docker，请先安装 Docker！(命令: curl -fsSL https://get.docker.com | sh)"
    exit 1
fi

# 创建持久化数据与上传文件夹
mkdir -p data uploads

# 构建并启动 Docker 容器
echo "📦 正在构建并启动 Docker 容器..."
docker compose down 2>/dev/null
docker compose up -d --build

echo "========================================================"
echo "🎉 自托管后端部署完成！"
echo "🌐 API 服务运行端口: http://你的服务器IP:8080"
echo "📊 健康检查端点: http://你的服务器IP:8080/api/v1/auth/login"
echo "========================================================"
