### 1. 技术栈架构 (Tech Stack)

- **核心框架**: React 18+ (Function Components + Hooks)
- **构建工具**: Vite (极速冷启动与 HMR)
- **开发语言**: TypeScript (强类型约束，提升代码健壮性)
- **样式引擎**: Tailwind CSS (原子化 CSS，便于快速构建响应式和深色模式)
- **图标系统**: Lucide React (风格统一、轻量级的 SVG 图标库)
- **路由/导航**: 基于状态的条件渲染 (react-router)
- **状态管理：**Zustand
- **缓存：**React Query

------



### 2. 昴云 UI 设计规范 (Nebula UI Design System)

本设计风格被定义为 **"Nebula Glassmorphism" (星云玻璃拟态)**，核心在于在深邃的暗色背景中，通过半透明磨砂材质、光晕和渐变色营造沉浸感。

#### 2.1 色彩体系 (Color Palette)

- **全局背景 (Global Background)**:
  - \#0f0c15 (深空黑) —— 用于 body 和页面主容器背景，营造无边界的深空感。
- **品牌主色 (Brand Primary)**:
  - \#6217d7 (星云紫) —— 用于主按钮、选中状态、高亮文本。
  - \#8a4af3 (亮紫色) —— 用于渐变色的尾部，增加色彩流动感。
- **文字颜色 (Typography Colors)**:
  - **高亮/标题**: text-white (100% 纯白)
  - **正文/图标**: text-white/80 (80% 透明度)
  - **占位符/次要信息**: text-white/40 或 text-white/30
- **功能色**:
  - 成功: bg-emerald-500
  - 错误: bg-red-600

#### 2.2 材质与光影 (Materials & Effects)

这是实现“玻璃拟态”的关键参数（Tailwind 类名）：

- **玻璃容器 (Glass Container)**:
  - 背景: bg-white/10 (10% 白色叠加)
  - 模糊: backdrop-blur-xl 或 backdrop-blur-2xl (强高斯模糊)
  - 边框: border border-white/10 (微弱的白色边框，模拟玻璃边缘反光)
  - 阴影: shadow-2xl 或 shadow-[0_8px_32px_0_rgba(0,0,0,0.37)]
- **输入框材质 (Input Fields)**:
  - 背景: bg-white/20 (比容器稍亮)
  - 交互: focus:bg-white/25 (聚焦时亮度提升)
- **氛围光晕 (Ambient Glow)**:
  - 使用绝对定位的 div 配合 bg-primary/60, blur-2xl, rounded-full 放置在卡片背后或 Logo 背后，制造发光效果。

#### 2.3 组件样式规则 (Component Styles)

- **按钮 (Buttons)**:
  - **主按钮**: 线性渐变背景 (bg-gradient-to-r from-[#6217d7] to-[#8a4af3])，带彩色阴影 (shadow-purple-900/30)。
  - **圆角**: 统一使用大圆角 rounded-xl 或 rounded-2xl，保持柔和视觉。
- **排版 (Typography)**:
  - **字体**: Roboto, sans-serif。
  - **标题特效**: 使用 bg-clip-text text-transparent bg-gradient-to-r from-white to-white/60 制作文字金属光泽。
  - **间距**: 强调宽松的留白 (p-8, gap-5)，避免密集排版。

#### 2.4 动效与交互 (Animation & Interaction)

- **入场动画**: 页面加载时使用 animate-in fade-in zoom-in-95 duration-700，模拟从远处浮现的感觉。
- **悬浮反馈 (Hover)**:
  - 按钮/图标: hover:scale-110 (轻微放大)。
  - 输入框: transition-all duration-300 (平滑的背景色和边框颜色过渡)。
- **环境动态**:
  - Logo/装饰物: 使用 animate-float (自定义 CSS keyframes) 实现上下缓慢浮动，模拟太空失重感。
  - 光晕: 使用 animate-pulse 实现呼吸灯效果。

#### 2.5 布局原则 (Layout Principles)

- **主页**: 侧边栏固定 (Fixed Sidebar) + 顶部导航 (Sticky Header) + 内容区滚动。内容区同样使用半透明背景层叠在深色底图之上。

#### 2.6 其他规则

- 封面展示使用经原图URL处理过的WEPB图URL，webp图URL与原图仅后缀不同，例如原图URL为xxx.jpg，webp图URL就是xxx.webp

