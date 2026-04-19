📁 目录规范
- components/：纯 UI 组件（函数式，默认导出，PascalCase.tsx）  
- services/：仅封装 fetch，返回 { code, data, message? }  
- stores/：Zustand 状态（认证、主题、Toast）  
- types/：类型定义  
- utils/：纯函数  
- config.ts：集中管理所有 /api/... 路径  

🎨 UI 与样式
- 风格：Nebula Glassmorphism  
  - 主色 #6217d7 → hover #4e12ac  
  - 卡片：bg-white/10~20 backdrop-blur rounded-xl  
  - 文字：标题 text-white，正文 text-white/80，辅助 text-white/40  
- 样式：用 Tailwind 或 styles.css，禁用行内 style  
- 动效：≤0.3s，复用现有动画类  
- 主题：通过 html[data-theme] 切换，通用样式抽离  

⚙️ 代码规范
- 禁用 any，API 响应必须类型收敛  
- 命名：事件 handleXxx，状态 isXxx/hasXxx  
- 性能：按需用 useMemo/useCallback  
- 组件：只用函数式组件，不引入外部 UI 库  

🔐 API 与安全
- 所有路径从 config.ts 引用  
- 鉴权请求带 credentials: 'include'  
- CSRF 头从 useAuthStore 获取  
- services/ 不含 UI 逻辑  

❗ 错误处理
- 业务错误 → Toast 提示  
- 禁止吞错，关键错误必须暴露  
- 禁止提交 console.log（可留 console.error）  

