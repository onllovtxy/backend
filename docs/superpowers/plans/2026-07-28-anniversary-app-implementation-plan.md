# 情侣专属纪念日 PWA 应用 (LoveEver) — 开发实施计划 (Implementation Plan)

> 本计划将设计规范拆解为具体的、可落地的开发步骤。

---

## 阶段一：项目初始化与基础设施搭建 (Phase 1: Project Setup)

- [ ] **Task 1.1**: 使用 Next.js 初始化脚手架
  - 安装 Next.js (App Router), TypeScript, TailwindCSS, Lucide Icons, Framer Motion, Zustand
  - 配置 `tsconfig.json` 别名 (`@/*`)

- [ ] **Task 1.2**: 配置 PWA 与全局主题
  - 配置 `public/manifest.json` 与 App 图标
  - 编写 `src/app/globals.css` 设计系统变量（柔粉渐变与暗色模式）

---

## 阶段二： Supabase 云端与数据服务层 (Phase 2: Data & State Layer)

- [ ] **Task 2.1**: Supabase 客户端与 Types 定义
  - 创建 `src/lib/supabase/client.ts`
  - 导出 TypeScript 接口类型 (`Couple`, `Profile`, `Anniversary`, `Memory`)
  - 提供本地 Mock / Fallback 模式（若暂未填入 Supabase API Key 时，支持使用 LocalStorage 沉浸式无缝体验）

- [ ] **Task 2.2**: 建立 Zustand 状态库
  - `useAuthStore` (用户与配对状态)
  - `useAnniversaryStore` (纪念日列表与实时订阅)
  - `useMemoryStore` (时光回忆相册)

---

## 阶段三：界面与功能模块开发 (Phase 3: UI & Feature Development)

- [ ] **Task 3.1**: 登录与邀请码配对页面 (`/auth`)
  - 手机号/邮箱注册登录表单
  - “我的邀请码”显示与复制 + “输入对方邀请码”绑定组件

- [ ] **Task 3.2**: 首页核心倒计时与 Hero 卡片 (`/`)
  - 恋爱天数大字号正计时卡片 (Count-up Timer)
  - 下一个重大纪念日倒计时 (Count-down Timer)
  - 浪漫粒子/心跳脉冲背景与导航栏

- [ ] **Task 3.3**: 纪念日管理列表与弹窗 (`/anniversaries`)
  - 纪念日列表卡片（支持正计时/倒计时标识）
  - 新增/编辑纪念日 Modal 弹窗（支持选择图标与日期）

- [ ] **Task 3.4**: 时光回忆照相馆 (`/memories`)
  - 时间轴图文卡片组件
  - 新增回忆与照片上传组件

- [ ] **Task 3.5**: 个人中心与设置 (`/profile`)
  - 双人头像与昵称展示
  - 解除绑定 / 数据导出备份按钮

---

## 阶段四：验证与本地运行测试 (Phase 4: Verification)

- [ ] **Task 4.1**: 本地启动开发服务器 `npm run dev` 并验证响应式布局
- [ ] **Task 4.2**: 检查控制台无 Error 与 Warning，确保 PWA 应用配置正常
