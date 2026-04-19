<div align="center">
  <img width="1200" height="475" alt="GHBanner" src="https://github.com/user-attachments/assets/0aa67016-6eaf-458a-adb2-6e31a0763ed6" />
</div>

# 昴云（subarupic）项目开发规则

本规则用于规范后续开发的技术选型、UI风格与代码习惯，降低维护成本并保证一致性。

## 1. 技术栈与约束

- 框架：React（Function Components + Hooks）
- 语言：TypeScript
- 构建：Vite
- 路由：react-router-dom（HashRouter）
- 样式：Tailwind CSS + `styles.css` 里的 CSS 变量主题
- 图标：lucide-react
- 状态管理：Zustand
- 测试：Vitest（jsdom）

约束：
- 不引入额外框架式 UI 库；优先复用/扩展 `components/` 现有组件
- 不在组件中直接写行内 style；优先 Tailwind，其次在 `styles.css` 增补通用样式
- 仅使用相对 API 路径（由开发代理/生产网关转发），不要在业务代码里写死后端域名

## 2. 运行、构建与测试

- 安装依赖：`npm install`
- 本地开发：`npm run dev`
- 打包构建：`npm run build`
- 本地预览：`npm run preview`
- 单测：`npm run test`
- 类型检查：`npx tsc -p tsconfig.json --noEmit`

开发代理：
- 约定所有接口以 `/api` 开头，开发环境由 Vite 代理到 `http://localhost:8124`（见 [vite.config.ts](vite.config.ts)）

## 3. 目录结构约定

- `components/`：UI 与页面模块（优先拆分可复用组件）
- `services/`：API 调用与数据适配层（统一 `fetch` 规范、统一返回值处理）
- `stores/`：Zustand 状态（认证、主题、toast 等）
- `types/`：领域类型定义（图片、空间、认证等）
- `utils/`：纯工具函数（不依赖 React）
- `static/`：静态资源
- `styles.css`：Tailwind 注入层 + 全局主题变量与通用动效
- `config.ts`：API 路由常量（新增接口必须先补充到这里）

## 4. UI 风格（Nebula Glassmorphism）

总体风格：深色宇宙背景 + 玻璃拟态半透明卡片 + 紫色品牌光晕。

必须遵守：
- 主题色：`#6217d7`（primary），hover：`#4e12ac`（见 [tailwind.config.cjs](tailwind.config.cjs) 与 [styles.css](styles.css)）
- 背景/卡片：暗色背景上叠加 `bg-white/10~20 + backdrop-blur` 的玻璃材质
- 文本层级：标题 `text-white`，正文 `text-white/80`，弱信息 `text-white/40`
- 圆角与阴影：组件统一 `rounded-xl/2xl`，阴影偏柔和并允许 `shadow-primary/xx` 光晕
- 动效：轻量、短时（0.2~0.3s），优先复用 `animate-fade-in` 等现有动效

参考规范：见 [design.md](prompt/design.md)

## 5. 代码风格与工程约定

### 5.1 TypeScript

- 不使用 `any` 逃逸类型；确需 `unknown` 时必须在边界处做收敛转换
- API 响应类型统一为 `{ code: number; data: T; message?: string }` 形态（见 `services/*`）
- 组件 Props 与领域类型放到 `types/` 或就近文件；避免重复定义

### 5.2 React 组件

- 仅使用函数组件与 Hooks；避免 class component
- 优先 `default export` 导出组件，文件名使用 `PascalCase.tsx`
- 事件处理函数以 `handleXxx` 命名；布尔状态以 `isXxx/hasXxx/canXxx` 命名
- 性能优化只在必要时引入：优先 `useMemo/useCallback`，避免过早优化

### 5.3 样式与布局

- Tailwind 类名优先写在 `className` 中，复用时抽到 `components/` 或 `styles.css` 的通用 utility
- 主题切换通过 `document.documentElement[data-theme]` 实现，不要新增第二套主题机制（见 [theme.ts](stores/theme.ts)）

### 5.4 API 与鉴权

- 所有接口路径统一从 [config.ts](config.ts) 的 `API_ROUTES` 引用
- 请求必须携带 `credentials: 'include'`（当接口依赖 cookie/session 时）
- 需要 CSRF 的接口统一从 `useAuthStore.getState().csrfHeader` 合并请求头（见 [auth.ts](stores/auth.ts) 与 [user.ts](services/user.ts)）
- `services/` 只做“网络请求 + 数据适配”，不要掺杂 UI 状态与组件逻辑

### 5.5 日志与错误处理

- 业务错误：优先通过 toast 反馈（见 `stores/toastStore.ts` 与 `components/Toast/`）
- 禁止在 UI 层吞错；必要时抛出或返回可判断结果，交给调用方展示
- 避免提交调试用 `console.log`（可在必要处保留少量 `console.error`）

## 6. 测试规则（Vitest）

- 新增 store / utils 的关键逻辑需补充单测（参考 [auth.test.ts](stores/auth.test.ts)）
- 单测目标：关键分支覆盖与可回归，不追求过度 mock UI

## 7. 配置与安全

- 不提交本地配置与密钥：`.env.local` 属于本地文件（已在 `.gitignore` 通过 `*.local` 忽略）
- Vite 环境变量使用 `VITE_` 前缀
- 认证加密使用 `VITE_AUTH_SECRET`（未配置时会使用开发默认值；生产环境必须配置）

## 8. Git 与交付规范

- 分支命名：`feat/xxx`、`fix/xxx`、`chore/xxx`
- 提交信息：`type(scope): message`（type 建议：feat/fix/refactor/chore/test）
- 合并前自检：至少通过 `npm run test` + 类型检查；避免把调试日志带入主分支
