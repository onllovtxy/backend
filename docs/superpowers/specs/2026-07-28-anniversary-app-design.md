# 情侣专属纪念日 PWA 应用 (LoveEver / 恋念) — 产品设计规范文档

* **文档版本**：v1.0.0
* **设计日期**：2026-07-28
* **状态**：待审阅 (Draft for User Approval)

---

## 1. 项目概述与核心目标

### 1.1 项目定位
一款专为情侣/伴侣打造的移动端优先（Mobile-First）、高颜值、支持双端实时同步的响应式 PWA 纪念日应用。

### 1.2 核心业务场景
* **双人关系绑定**：支持生成/输入“恋人专属邀请码”，完成两人账号的安全绑定。
* **恋爱天数与倒计时**：首页大字号实时计算相爱天数（正计时）及下一个重大纪念日（如百天、周年、生日）倒计时。
* **时光画廊 (Memory Wall)**：记录重要时刻的图文日记，支持按时间轴展示。
* **双端实时同步**：任何一方新增纪念日或更新回忆，另一方手机界面瞬间无感刷新。
* **PWA 桌面化体验**：支持移动端 Safari / Chrome“添加到主屏幕”，无需下载 App Store/应用商店即可独立运行。

---

## 2. 系统架构与技术选型

```
+-------------------------------------------------------------+
|                  移动端 PWA / 响应式 Web 前端               |
|  Next.js (App Router) + React + TailwindCSS + Framer Motion |
+------------------------------+------------------------------+
                               |
                   HTTPS / WebSocket (Realtime)
                               |
+------------------------------v------------------------------+
|                     Supabase 云端服务                       |
|  +----------------+  +------------------+  +-------------+  |
|  | Authentication |  | PostgreSQL (DB)  |  | Realtime WS |  |
|  +----------------+  +------------------+  +-------------+  |
|  | Storage (图片) |  | RLS 安全策略     |                 |
|  +----------------+  +------------------+                 |
+-------------------------------------------------------------+
```

### 2.1 技术栈汇总
* **前端框架**：Next.js 14+ (App Router, React 18, TypeScript)
* **样式与UI组件**：TailwindCSS + Lucide Icons + Framer Motion (细腻浪漫动画)
* **状态管理**：Zustand (轻量级本地状态管理)
* **后端与数据库**：Supabase (Auth, Postgres, Realtime, Storage)
* **PWA 部署**：`@ducanh2912/next-pwa` (离线 Service Worker + Manifest)

---

## 3. 数据库数据模型设计 (Database Schema)

### 3.1 账号与配对表 (`couples` & `users`)
```sql
-- 情侣配对表
CREATE TABLE couples (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  invite_code VARCHAR(8) UNIQUE NOT NULL, -- 8位邀请码
  pair_date DATE NOT NULL,                -- 恋爱开始日期
  created_at TIMESTAMP WITH TIMEZONE DEFAULT now()
);

-- 用户拓展信息表
CREATE TABLE profiles (
  id UUID PRIMARY KEY REFERENCES auth.users(id) ON DELETE CASCADE,
  couple_id UUID REFERENCES couples(id),
  display_name VARCHAR(50) NOT NULL,
  avatar_url TEXT,
  role VARCHAR(10) CHECK (role IN ('partner_a', 'partner_b')),
  created_at TIMESTAMP WITH TIMEZONE DEFAULT now()
);
```

### 3.2 纪念日表 (`anniversaries`)
```sql
CREATE TABLE anniversaries (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  couple_id UUID REFERENCES couples(id) ON DELETE CASCADE NOT NULL,
  title VARCHAR(100) NOT NULL,            -- 纪念日标题（如：第一次看电影）
  target_date DATE NOT NULL,              -- 纪念日日期
  is_pinned BOOLEAN DEFAULT false,        -- 是否置顶首页
  icon VARCHAR(50) DEFAULT 'heart',       -- 图标类型
  background_url TEXT,                    -- 背景图
  created_by UUID REFERENCES auth.users(id),
  created_at TIMESTAMP WITH TIMEZONE DEFAULT now()
);
```

### 3.3 时光回忆日记表 (`memories`)
```sql
CREATE TABLE memories (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  couple_id UUID REFERENCES couples(id) ON DELETE CASCADE NOT NULL,
  title VARCHAR(150) NOT NULL,
  content TEXT,
  memory_date DATE NOT NULL,
  image_urls TEXT[],                       -- 照片数组
  created_by UUID REFERENCES auth.users(id),
  created_at TIMESTAMP WITH TIMEZONE DEFAULT now()
);
```

---

## 4. 界面与功能模块规划 (UI/UX Modules)

### 4.1 页面路由结构
1. `/auth` — 注册/登录与邀请码配对页
2. `/` (首页) — 恋爱天数大卡片 + 置顶纪念日倒计时 + 浪漫动态背景
3. `/anniversaries` — 所有纪念日列表（支持分类过滤与添加/编辑）
4. `/memories` — 时光照片墙与时间轴回忆录
5. `/profile` — 双人资料卡片、主题色设置、导出备份与取消绑定

### 4.2 视觉风格设计 (Design Aesthetic)
* **配色方案**：浪漫柔和色调（柔粉渐变 `#FF7E95` ↔ `#FFB085`，暗夜莫兰迪色系适配深色模式）。
* **交互体验**：卡片悬浮/滑动微动画、相爱天数数字动态滚动计数、心跳脉冲动画。

---

## 5. 关键业务流程与安全机制

### 5.1 二人配对流程 (Pairing Flow)
1. 用户 A 注册登录后生成唯一的 8 位邀请码（如 `LOVE-8899`）。
2. 用户 B 注册登录，选择“输入对方邀请码”。
3. 校验通过后，更新两人 profile 中的 `couple_id`，双方完成绑定并自动进入情侣主页。

### 5.2 数据安全与隔离 (Row Level Security - RLS)
* Supabase Postgres 开启 **RLS** 策略。
* 保证每个用户只能读写其 `couple_id` 匹配的数据，从数据库底层杜绝越权与数据泄露。

---

## 6. 自查与质量保证 (Spec Self-Review)

* ✅ **占位符检查**：已消除所有 TBD/TODO，所有字段类型与表结构清晰明确。
* ✅ **一致性检查**：前端 PWA 与 Supabase Realtime WebSocket 协议完全匹配。
* ✅ **范围控制 (Scope Check)**：MVP 版本聚焦于“配对、纪念日倒计时、回忆相册”，避免引入过度复杂的社交与消费模块。
