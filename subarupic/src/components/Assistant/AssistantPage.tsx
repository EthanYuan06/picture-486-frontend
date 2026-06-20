import React, {
  useCallback,
  useEffect,
  useMemo,
  useRef,
  useState,
} from 'react';
import ReactMarkdown from 'react-markdown';
import remarkGfm from 'remark-gfm';
import {
  Bot,
  Menu,
  MessageSquarePlus,
} from 'lucide-react';
import { useAuthStore } from '../../stores/auth';
import { useThemeStore } from '../../stores/theme';

type AssistantRole = 'user' | 'assistant';

interface UploadedImage {
  id: string;
  name: string;
  url: string;
}

interface AssistantMessage {
  id: string;
  role: AssistantRole;
  content: string;
  createdAt: number;
  attachments: UploadedImage[];
}

interface ChatSession {
  id: string;
  title: string;
  preview: string;
  updatedAt: number;
  messages: AssistantMessage[];
}

const createId = (): string =>
  `${Date.now().toString(36)}-${Math.random().toString(36).slice(2, 8)}`;

const extractPlainText = (content: string): string =>
  content
    .replace(/!\[.*?\]\(.*?\)/g, '')
    .replace(/\[([^\]]+)\]\((.*?)\)/g, '$1')
    .replace(/[#>*`~-]/g, ' ')
    .replace(/\s+/g, ' ')
    .trim();

const formatTime = (timestamp: number): string =>
  new Intl.DateTimeFormat('zh-CN', {
    hour: '2-digit',
    minute: '2-digit',
  }).format(timestamp);

const buildWelcomeMessage = (): AssistantMessage => ({
  id: createId(),
  role: 'assistant',
  createdAt: Date.now(),
  attachments: [],
  content: [
    '### 紫穹 AI 智能助手',
    '欢迎回来，我会用更偏 **现代科技紫渐变** 的视觉语言协助你梳理需求、生成文案或整理灵感。',
    '',
    '- 支持 Markdown 回复与结构化建议',
    '- 可上传图片作为参考素材',
    '- 支持快捷键：`Enter` 发送、`Shift + Enter` 换行、`Esc` 聚焦输入框',
  ].join('\n'),
});

const buildReplyContent = (
  prompt: string,
  imageCount: number,
  sessionTitle: string,
): string => {
  const normalizedPrompt = prompt.trim() || '这组视觉参考';
  const imageLine =
    imageCount > 0
      ? `- 已识别到 **${imageCount} 张图片参考**，建议优先统一主体光效、材质语言与镜头景深。`
      : '- 当前没有附图，我会优先基于文本意图拆解任务。';

  return [
    `### 已接收「${sessionTitle}」`,
    '',
    `我已读取你的输入：**${normalizedPrompt}**`,
    '',
    '#### 推荐执行路径',
    '- 明确目标：先锁定受众、任务场景与需要强化的视觉重点。',
    '- 拆分内容：将主诉求、交互反馈、信息层级分成独立模块逐步细化。',
    '- 输出结果：需要时我可以继续生成界面文案、组件结构或视觉提示词。',
    '',
    '#### 当前判断',
    imageLine,
    '- 这类对话页适合使用深色低饱和背景、局部紫色辉光与轻量玻璃卡片，避免强霓虹造成疲劳。',
    '- 用户消息建议保持高饱和渐变强调，AI 回复保持半透明面板与较弱外发光，增强角色辨识度。',
    '',
    '#### 下一步我可以继续',
    '1. 帮你润色这段需求为更完整的产品说明。',
    '2. 生成更适合前端实现的交互清单。',
    '3. 将内容继续细化为提示词、文案或组件结构。',
  ].join('\n');
};

const createSeedSessions = (): ChatSession[] => {
  const now = Date.now();
  const welcome = buildWelcomeMessage();

  return [
    {
      id: createId(),
      title: 'AI 助手体验',
      preview: '欢迎回来，我会用更偏现代科技紫渐变的视觉语言协助你。',
      updatedAt: now - 1000 * 60 * 3,
      messages: [
        {
          ...welcome,
          createdAt: now - 1000 * 60 * 7,
        },
        {
          id: createId(),
          role: 'user',
          createdAt: now - 1000 * 60 * 5,
          attachments: [],
          content: '帮我把首页入口做成更有科技感的机器人图标，并且跳转到 AI 对话页。',
        },
        {
          id: createId(),
          role: 'assistant',
          createdAt: now - 1000 * 60 * 3,
          attachments: [],
          content: [
            '当然可以，我建议这样处理：',
            '',
            '- 入口放在头部中间留白区，优先保证可见性与品牌辨识度。',
            '- 图标外层使用 **深紫到浅紫渐变圆角胶囊**，内部搭配简洁机器人头像。',
            '- Hover 只做轻微上浮与阴影增强，避免过度霓虹。',
          ].join('\n'),
        },
      ],
    },
    {
      id: createId(),
      title: '角色设定讨论',
      preview: '上传两张参考图后，我会帮你统一角色视觉语言。',
      updatedAt: now - 1000 * 60 * 42,
      messages: [
        {
          ...welcome,
          createdAt: now - 1000 * 60 * 54,
        },
        {
          id: createId(),
          role: 'assistant',
          createdAt: now - 1000 * 60 * 42,
          attachments: [],
          content: [
            '如果你准备了角色参考图，可以直接上传。',
            '',
            '- 我会总结共同点',
            '- 提炼适合页面的配色和氛围',
            '- 继续生成更统一的视觉提示词',
          ].join('\n'),
        },
      ],
    },
  ];
};

const createFreshSession = (sessionId?: string): ChatSession => {
  const welcomeMessage = buildWelcomeMessage();
  return {
    id: sessionId ?? createId(),
    title: '新会话',
    preview: '准备开始新的灵感对话',
    updatedAt: welcomeMessage.createdAt,
    messages: [welcomeMessage],
  };
};

const RobotGlyph: React.FC<{ compact?: boolean }> = ({ compact = false }) => (
  <span
    aria-hidden="true"
    className={`relative inline-flex items-center justify-center rounded-2xl bg-gradient-to-br from-[#6B46C1] to-[#9F7AEA] text-white shadow-[0_14px_30px_rgba(107,70,193,0.28)] ${
      compact ? 'h-9 w-9' : 'h-10 w-10'
    }`}
  >
    <Bot className={compact ? 'h-4 w-4' : 'h-5 w-5'} />
    <span className="absolute right-1.5 top-1.5 h-1.5 w-1.5 rounded-full bg-[#D6BCFA]" />
  </span>
);

const AssistantPage: React.FC = () => {
  const userInfo = useAuthStore((state) => state.userInfo);
  const theme = useThemeStore((state) => state.theme);
  const isDarkTheme = theme === 'dark';
  const initialSessionsRef = useRef<ChatSession[]>(createSeedSessions());
  const latestSessionsRef = useRef<ChatSession[]>(initialSessionsRef.current);
  const latestDraftImagesRef = useRef<UploadedImage[]>([]);
  const [sessions, setSessions] = useState<ChatSession[]>(initialSessionsRef.current);
  const [activeSessionId, setActiveSessionId] = useState<string>(
    initialSessionsRef.current[0].id,
  );
  const [draft, setDraft] = useState('');
  const [draftImages, setDraftImages] = useState<UploadedImage[]>([]);
  const [isReplying, setIsReplying] = useState(false);
  const [isSidebarOpen, setIsSidebarOpen] = useState(false);
  const [prefersReducedMotion, setPrefersReducedMotion] = useState(false);
  const textareaRef = useRef<HTMLTextAreaElement>(null);
  const bottomRef = useRef<HTMLDivElement>(null);
  const replyTimerRef = useRef<number | null>(null);

  const activeSession = useMemo(
    () => sessions.find((session) => session.id === activeSessionId) ?? sessions[0],
    [activeSessionId, sessions],
  );

  useEffect(() => {
    latestSessionsRef.current = sessions;
  }, [sessions]);

  useEffect(() => {
    latestDraftImagesRef.current = draftImages;
  }, [draftImages]);

  const revokeImages = useCallback((images: UploadedImage[]) => {
    images.forEach((image) => {
      if (image.url.startsWith('blob:')) {
        URL.revokeObjectURL(image.url);
      }
    });
  }, []);

  const closePendingReply = useCallback(() => {
    if (replyTimerRef.current !== null) {
      window.clearTimeout(replyTimerRef.current);
      replyTimerRef.current = null;
    }
    setIsReplying(false);
  }, []);

  useEffect(() => {
    const media = window.matchMedia('(prefers-reduced-motion: reduce)');
    const updateMotionPreference = () => setPrefersReducedMotion(media.matches);

    updateMotionPreference();
    media.addEventListener('change', updateMotionPreference);

    return () => {
      media.removeEventListener('change', updateMotionPreference);
    };
  }, []);

  useEffect(() => {
    if (!activeSession) {
      return;
    }

    bottomRef.current?.scrollIntoView({
      behavior: prefersReducedMotion ? 'auto' : 'smooth',
      block: 'end',
    });
  }, [activeSession, isReplying, prefersReducedMotion]);

  useEffect(() => {
    if (!textareaRef.current) {
      return;
    }

    textareaRef.current.style.height = 'auto';
    textareaRef.current.style.height = `${Math.min(textareaRef.current.scrollHeight, 160)}px`;
  }, [draft]);

  useEffect(() => {
    const handleGlobalKeydown = (event: KeyboardEvent) => {
      if (event.key === 'Escape') {
        textareaRef.current?.focus();
        setIsSidebarOpen(false);
      }
    };

    window.addEventListener('keydown', handleGlobalKeydown);
    return () => {
      window.removeEventListener('keydown', handleGlobalKeydown);
    };
  }, []);

  useEffect(() => {
    return () => {
      closePendingReply();
      revokeImages(latestDraftImagesRef.current);
      latestSessionsRef.current.forEach((session) => {
        session.messages.forEach((message) => revokeImages(message.attachments));
      });
    };
  }, [closePendingReply, revokeImages]);

  const updateSessionById = useCallback(
    (sessionId: string, updater: (session: ChatSession) => ChatSession) => {
      setSessions((currentSessions) =>
        currentSessions.map((session) =>
          session.id === sessionId ? updater(session) : session,
        ),
      );
    },
    [],
  );

  const createSessionTitle = useCallback((value: string): string => {
    const plainText = extractPlainText(value);
    return plainText ? plainText.slice(0, 18) : '图像参考会话';
  }, []);

  const createSessionPreview = useCallback(
    (value: string, imageCount: number): string => {
      const plainText = extractPlainText(value);
      if (plainText) {
        return plainText.slice(0, 36);
      }
      if (imageCount > 0) {
        return `上传了 ${imageCount} 张图片参考`;
      }
      return '等待继续输入';
    },
    [],
  );

  const handleCreateSession = useCallback(() => {
    closePendingReply();
    revokeImages(draftImages);
    setDraft('');
    setDraftImages([]);

    const session = createFreshSession();
    setSessions((currentSessions) => [session, ...currentSessions]);
    setActiveSessionId(session.id);
    setIsSidebarOpen(false);
    window.setTimeout(() => textareaRef.current?.focus(), 0);
  }, [closePendingReply, draftImages, revokeImages]);

  const handleDeleteSession = useCallback(
    (sessionId: string) => {
      const sessionIndex = sessions.findIndex((session) => session.id === sessionId);
      const session = sessions[sessionIndex];

      if (!session) {
        return;
      }

      if (sessions.length === 1) {
        const resetSession = createFreshSession();
        closePendingReply();
        session.messages.forEach((message) => revokeImages(message.attachments));
        revokeImages(draftImages);
        setDraft('');
        setDraftImages([]);
        setSessions([resetSession]);
        setActiveSessionId(resetSession.id);
        return;
      }

      if (sessionId === activeSessionId) {
        closePendingReply();
        revokeImages(draftImages);
        setDraft('');
        setDraftImages([]);
        const nextSession =
          sessions[sessionIndex + 1] ?? sessions[Math.max(sessionIndex - 1, 0)];
        setActiveSessionId(nextSession.id);
      }

      session.messages.forEach((message) => revokeImages(message.attachments));
      setSessions((currentSessions) =>
        currentSessions.filter((currentSession) => currentSession.id !== sessionId),
      );
    },
    [
      activeSessionId,
      closePendingReply,
      draftImages,
      revokeImages,
      sessions,
    ],
  );

  const handleClearConversation = useCallback(() => {
    if (!activeSession) {
      return;
    }

    closePendingReply();
    revokeImages(draftImages);
    setDraft('');
    setDraftImages([]);

    activeSession.messages.forEach((message) => revokeImages(message.attachments));
    updateSessionById(activeSession.id, () => createFreshSession(activeSession.id));
    window.setTimeout(() => textareaRef.current?.focus(), 0);
  }, [
    activeSession,
    closePendingReply,
    draftImages,
    revokeImages,
    updateSessionById,
  ]);

  const handleSelectImages = useCallback(
    (event: React.ChangeEvent<HTMLInputElement>) => {
      const files = Array.from(event.target.files ?? []);
      if (files.length === 0) {
        return;
      }

      const nextImages = files.map((file) => ({
        id: createId(),
        name: file.name,
        url: URL.createObjectURL(file),
      }));

      setDraftImages((currentImages) => [...currentImages, ...nextImages]);
      event.target.value = '';
    },
    [],
  );

  const handleRemoveDraftImage = useCallback(
    (imageId: string) => {
      setDraftImages((currentImages) => {
        const target = currentImages.find((image) => image.id === imageId);
        if (target) {
          revokeImages([target]);
        }
        return currentImages.filter((image) => image.id !== imageId);
      });
    },
    [revokeImages],
  );

  const handleSend = useCallback(() => {
    if (!activeSession || isReplying) {
      return;
    }

    const trimmedDraft = draft.trim();
    if (!trimmedDraft && draftImages.length === 0) {
      return;
    }

    const imagesForMessage = draftImages;
    const fallbackMessage =
      draftImages.length > 0 ? `请分析这 ${draftImages.length} 张参考图。` : '';
    const messageText = trimmedDraft || fallbackMessage;
    const now = Date.now();

    const userMessage: AssistantMessage = {
      id: createId(),
      role: 'user',
      content: messageText,
      createdAt: now,
      attachments: imagesForMessage,
    };

    const nextTitle =
      activeSession.title === '新会话'
        ? createSessionTitle(messageText)
        : activeSession.title;
    const nextPreview = createSessionPreview(messageText, imagesForMessage.length);

    updateSessionById(activeSession.id, (session) => ({
      ...session,
      title: nextTitle,
      preview: nextPreview,
      updatedAt: now,
      messages: [...session.messages, userMessage],
    }));

    setDraft('');
    setDraftImages([]);
    setIsReplying(true);
    setIsSidebarOpen(false);

    const sessionId = activeSession.id;
    replyTimerRef.current = window.setTimeout(() => {
      const assistantMessage: AssistantMessage = {
        id: createId(),
        role: 'assistant',
        content: buildReplyContent(messageText, imagesForMessage.length, nextTitle),
        createdAt: Date.now(),
        attachments: [],
      };

      updateSessionById(sessionId, (session) => ({
        ...session,
        preview: extractPlainText(assistantMessage.content).slice(0, 36),
        updatedAt: assistantMessage.createdAt,
        messages: [...session.messages, assistantMessage],
      }));

      setIsReplying(false);
      replyTimerRef.current = null;
    }, prefersReducedMotion ? 120 : 1100);

    window.setTimeout(() => textareaRef.current?.focus(), 0);
  }, [
    activeSession,
    createSessionPreview,
    createSessionTitle,
    draft,
    draftImages,
    isReplying,
    prefersReducedMotion,
    updateSessionById,
  ]);

  const handleComposerKeyDown = useCallback(
    (event: React.KeyboardEvent<HTMLTextAreaElement>) => {
      if (
        event.key === 'Enter' &&
        !event.shiftKey &&
        !event.nativeEvent.isComposing
      ) {
        event.preventDefault();
        handleSend();
      }
    },
    [handleSend],
  );

  if (!activeSession) {
    return null;
  }

  const shellClass = isDarkTheme
    ? 'border-white/10 bg-[#121212] text-white shadow-[0_24px_90px_rgba(8,8,18,0.48)]'
    : 'border-slate-200/90 bg-[#F7F5FF] text-slate-900 shadow-[0_24px_90px_rgba(148,163,184,0.2)]';
  const shellGlowClass = isDarkTheme
    ? 'bg-[radial-gradient(circle_at_top,_rgba(159,122,234,0.18),_transparent_42%),radial-gradient(circle_at_bottom_right,_rgba(107,70,193,0.16),_transparent_32%)]'
    : 'bg-[radial-gradient(circle_at_top,_rgba(159,122,234,0.18),_transparent_40%),radial-gradient(circle_at_bottom_right,_rgba(99,179,237,0.12),_transparent_28%)]';
  const overlayClass = isDarkTheme ? 'bg-black/50' : 'bg-slate-900/20';
  const sidebarClass = isDarkTheme
    ? 'border-white/10 bg-[#151515]/95'
    : 'border-slate-200/90 bg-white/88 shadow-[18px_0_40px_rgba(148,163,184,0.12)]';
  const sidebarHeaderTextClass = isDarkTheme ? 'text-white' : 'text-slate-900';
  const sidebarActionClass = isDarkTheme
    ? 'border-white/10 bg-white/5 text-[#D6BCFA] hover:bg-white/10 hover:text-white'
    : 'border-slate-200 bg-[#F5F3FF] text-[#6B46C1] hover:bg-[#EDE9FE] hover:text-[#4C1D95]';
  const inactiveSessionCardClass = isDarkTheme
    ? 'border-white/10 bg-white/[0.03]'
    : 'border-slate-200/80 bg-white/82';
  const activeSessionPanelClass = isDarkTheme ? 'bg-[#191622]' : 'bg-[#FCFBFF]';
  const inactiveSessionIconClass = isDarkTheme
    ? 'bg-[#201C2C] text-[#D6BCFA]'
    : 'bg-[#F3E8FF] text-[#6B46C1]';
  const sessionTitleClass = isDarkTheme ? 'text-white' : 'text-slate-900';
  const topHeaderClass = isDarkTheme
    ? 'border-white/10 bg-[#121212]/80'
    : 'border-slate-200/80 bg-white/72';
  const mobileSidebarButtonClass = isDarkTheme
    ? 'border-white/10 bg-white/5 text-white hover:bg-white/10'
    : 'border-slate-200 bg-white/88 text-slate-700 hover:bg-slate-50';
  const titleClass = isDarkTheme ? 'text-white' : 'text-slate-900';
  const subtitleClass = isDarkTheme ? 'text-[#94A3B8]' : 'text-slate-600';
  const assistantIconClass = isDarkTheme
    ? 'border border-white/10 bg-white/[0.08] text-[#D6BCFA] shadow-[0_0_32px_rgba(107,70,193,0.22)]'
    : 'border border-[#D8B4FE]/60 bg-white text-[#6B46C1] shadow-[0_12px_32px_rgba(196,181,253,0.34)]';
  const assistantBubbleClass = isDarkTheme
    ? 'rounded-bl-md border border-white/12 bg-white/[0.07] text-[#E2E8F0] shadow-[0_0_0_1px_rgba(255,255,255,0.03),0_0_36px_rgba(107,70,193,0.16)]'
    : 'rounded-bl-md border border-white/80 bg-white/78 text-slate-700 shadow-[0_18px_40px_rgba(148,163,184,0.16),0_0_0_1px_rgba(255,255,255,0.8)]';
  const assistantMetaClass = isDarkTheme ? 'text-[#94A3B8]' : 'text-slate-500';
  const attachmentCardClass = isDarkTheme
    ? 'border-white/10 bg-[#181818] hover:shadow-[0_18px_34px_rgba(15,23,42,0.35)]'
    : 'border-slate-200 bg-white hover:shadow-[0_18px_34px_rgba(148,163,184,0.2)]';
  const attachmentCaptionClass = isDarkTheme ? 'text-[#CBD5E1]' : 'text-slate-600';
  const composerWrapClass = isDarkTheme
    ? 'border-t border-white/10 bg-[#121212]/90'
    : 'border-t border-slate-200/80 bg-white/72';
  const draftCardClass = isDarkTheme
    ? 'border-white/10 bg-white/[0.04] hover:shadow-[0_18px_34px_rgba(15,23,42,0.35)]'
    : 'border-slate-200 bg-white hover:shadow-[0_18px_34px_rgba(148,163,184,0.2)]';
  const removeImageButtonClass = isDarkTheme
    ? 'bg-black/45 text-white hover:bg-black/65'
    : 'bg-slate-900/10 text-slate-700 hover:bg-slate-900/20';
  const composerOuterClass = isDarkTheme
    ? 'border-white/10 bg-white/[0.04] shadow-[0_18px_48px_rgba(6,6,12,0.35)]'
    : 'border-slate-200/90 bg-white/70 shadow-[0_18px_48px_rgba(148,163,184,0.18)] group-focus-within:border-[#9F7AEA]/55 group-focus-within:shadow-[0_0_0_3px_rgba(159,122,234,0.14),0_18px_48px_rgba(148,163,184,0.18)]';
  const composerInnerClass = isDarkTheme
    ? 'bg-[#18181B]/95'
    : 'bg-[#FFFFFF]/96';
  const composerLabelClass = isDarkTheme ? 'text-[#CBD5E1]' : 'text-slate-600';
  const composerTextareaClass = isDarkTheme
    ? 'text-white placeholder:text-[#94A3B8]'
    : 'text-slate-900 placeholder:text-slate-400';
  const secondaryButtonClass = isDarkTheme
    ? 'border-white/10 bg-white/[0.05] text-[#E2E8F0] hover:bg-white/[0.1]'
    : 'border-slate-200 bg-slate-50 text-slate-700 hover:bg-slate-100';
  const inputPanelTextClass = isDarkTheme ? 'text-[#CBD5E1]' : 'text-slate-600';

  return (
    <section className={`relative h-full min-h-0 overflow-hidden rounded-[30px] border ${shellClass}`}>
      <a
        href="#assistant-composer"
        className="sr-only focus:not-sr-only focus:absolute focus:left-6 focus:top-4 focus:z-50 focus:rounded-full focus:bg-white focus:px-4 focus:py-2 focus:text-sm focus:font-medium focus:text-slate-900"
      >
        跳到消息输入
      </a>

      <div className="assistant-particle-field pointer-events-none absolute inset-0">
        {Array.from({ length: 14 }).map((_, index) => (
          <span key={index} className={`assistant-particle assistant-particle-${index + 1}`} />
        ))}
      </div>
      <div className={`pointer-events-none absolute inset-0 ${shellGlowClass}`} />

      <div className="relative flex h-full">
        {isSidebarOpen && (
          <button
            type="button"
            aria-label="关闭会话侧边栏遮罩"
            className={`absolute inset-0 z-30 backdrop-blur-sm lg:hidden ${overlayClass}`}
            onClick={() => setIsSidebarOpen(false)}
          />
        )}

        <aside
          className={`absolute inset-y-0 left-0 z-40 flex w-[292px] flex-col border-r backdrop-blur-xl transition-transform duration-200 motion-reduce:transition-none lg:static lg:z-0 lg:translate-x-0 ${sidebarClass} ${
            isSidebarOpen ? 'translate-x-0' : '-translate-x-full'
          }`}
          aria-label="历史会话列表"
        >
          <div className={`flex items-center justify-between border-b px-4 py-4 ${isDarkTheme ? 'border-white/10' : 'border-slate-200/80'}`}>
            <div className="flex items-center gap-3">
              <RobotGlyph />
              <div className={`text-sm font-semibold ${sidebarHeaderTextClass}`}>云图库智能助手</div>
            </div>
            <button
              type="button"
              onClick={handleCreateSession}
              className={`inline-flex h-9 w-9 items-center justify-center rounded-xl border transition-colors duration-200 focus:outline-none focus:ring-2 focus:ring-[#9F7AEA] ${sidebarActionClass}`}
              aria-label="新建会话"
            >
              <MessageSquarePlus className="h-4 w-4" />
            </button>
          </div>

          <div className="flex-1 space-y-2 overflow-y-auto px-3 py-3">
            {sessions.map((session) => {
              const isActive = session.id === activeSession.id;
              return (
                <button
                  key={session.id}
                  type="button"
                  onClick={() => {
                    setActiveSessionId(session.id);
                    setIsSidebarOpen(false);
                  }}
                  className={`flex w-full min-w-0 items-center gap-3 rounded-2xl border px-3 py-2.5 text-left transition-colors duration-200 motion-reduce:transition-none ${
                    isActive
                      ? isDarkTheme
                        ? 'border-white/10 bg-white/[0.08]'
                        : 'border-[#C4B5FD]/50 bg-[#F5F3FF]'
                      : inactiveSessionCardClass
                  }`}
                >
                  <span
                    className={`mt-0.5 inline-flex h-8 w-8 flex-shrink-0 items-center justify-center rounded-xl ${
                      isActive
                        ? isDarkTheme
                          ? 'bg-white/10 text-white'
                          : 'bg-gradient-to-br from-[#6B46C1] to-[#9F7AEA] text-white'
                        : inactiveSessionIconClass
                    }`}
                  >
                    <Bot className="h-4 w-4" />
                  </span>
                  <span className={`line-clamp-1 min-w-0 flex-1 text-[13px] font-semibold leading-5 ${sessionTitleClass}`}>
                    {session.title}
                  </span>
                </button>
              );
            })}
          </div>
        </aside>

        <div className="flex min-w-0 flex-1 flex-col">
          <header className={`border-b px-4 py-3 backdrop-blur-xl sm:px-6 ${topHeaderClass}`}>
            <div className="flex items-center justify-between gap-4">
              <div className="flex min-w-0 items-center gap-3">
                <button
                  type="button"
                  onClick={() => setIsSidebarOpen(true)}
                  className={`inline-flex h-10 w-10 items-center justify-center rounded-2xl border transition-colors duration-200 focus:outline-none focus:ring-2 focus:ring-[#9F7AEA] lg:hidden ${mobileSidebarButtonClass}`}
                  aria-label="打开会话侧边栏"
                >
                  <Menu className="h-4 w-4" />
                </button>
                <h2
                  className={`truncate text-base font-semibold leading-none sm:text-lg ${titleClass}`}
                >
                  {activeSession.title}
                </h2>
              </div>

              <div className="hidden sm:flex" />
            </div>
          </header>

          <main
            id="assistant-main"
            className="flex min-h-0 flex-1 flex-col overflow-hidden"
          >
            <div className="flex-1 overflow-y-auto px-4 py-4 sm:px-6 lg:px-8">
              <div className="mx-auto flex w-full max-w-4xl flex-col gap-4 pb-14">
                {activeSession.messages.map((message) => {
                  const isUser = message.role === 'user';

                  return (
                    <article
                      key={message.id}
                      className={`flex ${isUser ? 'justify-end' : 'justify-start'}`}
                    >
                      <div
                        className={`flex max-w-[92%] items-start gap-3 sm:max-w-[84%] ${
                          isUser ? 'flex-row-reverse' : 'flex-row'
                        }`}
                      >
                        <span
                          className={`mt-1 inline-flex h-10 w-10 flex-shrink-0 items-center justify-center rounded-2xl ${
                            isUser
                              ? 'overflow-hidden bg-gradient-to-br from-[#6B46C1] to-[#9F7AEA] text-white'
                              : assistantIconClass
                          }`}
                        >
                          {isUser ? (
                            userInfo?.avatarUrl ? (
                              <img
                                src={userInfo.avatarUrl}
                                alt="用户头像"
                                className="h-full w-full object-cover"
                              />
                            ) : (
                              '你'
                            )
                          ) : (
                            <Bot className="h-4 w-4" />
                          )}
                        </span>

                        <div
                          className={`min-w-0 rounded-[24px] px-4 py-4 shadow-[0_18px_40px_rgba(0,0,0,0.16)] ${
                            isUser
                              ? 'rounded-br-md bg-gradient-to-br from-[#6B46C1] to-[#9F7AEA] text-white'
                              : `backdrop-blur-md ${assistantBubbleClass}`
                          }`}
                        >
                          <div
                            className={`mb-2 flex items-center gap-2 text-[11px] ${
                              isUser ? 'justify-end text-white/75' : assistantMetaClass
                            }`}
                          >
                            <span>{isUser ? '用户消息' : 'AI 助手'}</span>
                            <span>·</span>
                            <time dateTime={new Date(message.createdAt).toISOString()}>
                              {formatTime(message.createdAt)}
                            </time>
                          </div>

                          <div
                            className={`assistant-markdown text-sm leading-7 ${
                              isUser
                                ? 'assistant-markdown-user'
                                : isDarkTheme
                                  ? ''
                                  : 'assistant-markdown-light'
                            }`}
                          >
                            <ReactMarkdown remarkPlugins={[remarkGfm]}>
                              {message.content}
                            </ReactMarkdown>
                          </div>

                          {message.attachments.length > 0 && (
                            <div className="mt-4 grid grid-cols-2 gap-3 sm:grid-cols-3">
                              {message.attachments.map((image) => (
                                <figure
                                  key={image.id}
                                  className={`group overflow-hidden rounded-2xl border transition-all duration-200 motion-reduce:transition-none hover:-translate-y-1 motion-reduce:hover:translate-y-0 ${attachmentCardClass}`}
                                >
                                  <img
                                    src={image.url}
                                    alt={image.name}
                                    className="h-28 w-full object-cover transition-transform duration-200 motion-reduce:transition-none group-hover:scale-[1.03] motion-reduce:group-hover:scale-100"
                                  />
                                  <figcaption className={`truncate px-3 py-2 text-xs ${attachmentCaptionClass}`}>
                                    {image.name}
                                  </figcaption>
                                </figure>
                              ))}
                            </div>
                          )}
                        </div>
                      </div>
                    </article>
                  );
                })}

                {isReplying && (
                  <article className="flex justify-start">
                    <div className="flex max-w-[92%] items-start gap-3 sm:max-w-[84%]">
                      <span className={`mt-1 inline-flex h-10 w-10 flex-shrink-0 items-center justify-center rounded-2xl ${assistantIconClass}`}>
                        <Bot className="h-4 w-4" />
                      </span>
                      <div className={`rounded-[24px] rounded-bl-md px-4 py-4 backdrop-blur-md ${assistantBubbleClass}`}>
                        <div className={`mb-2 flex items-center gap-2 text-[11px] ${assistantMetaClass}`}>
                          <span>AI 助手</span>
                          <span>·</span>
                          <span>正在组织回复</span>
                        </div>
                        <div className={`flex items-center gap-3 text-sm ${isDarkTheme ? 'text-[#E2E8F0]' : 'text-slate-700'}`}>
                          <span className={`assistant-halo-spinner ${isDarkTheme ? '' : 'assistant-halo-spinner-light'}`} aria-hidden="true" />
                          <span className="assistant-typing-dots" aria-live="polite">
                            正在分析你的输入
                          </span>
                        </div>
                      </div>
                    </div>
                  </article>
                )}

                <div ref={bottomRef} />
              </div>
            </div>

            <div className={`px-4 py-3 backdrop-blur-xl sm:px-6 ${composerWrapClass}`}>
              <div className="mx-auto w-full max-w-4xl">
                <div className={`group relative rounded-[28px] border p-[1px] ${composerOuterClass}`}>
                  <div className={`pointer-events-none absolute inset-0 rounded-[28px] bg-gradient-to-r from-[#6B46C1] via-[#9F7AEA] to-[#63B3ED] opacity-0 blur-sm transition-opacity duration-200 group-focus-within:opacity-100 motion-reduce:transition-none ${isDarkTheme ? '' : 'hidden'}`} />
                  <div className={`relative rounded-[27px] border border-transparent px-4 py-3 ${composerInnerClass}`}>
                    <textarea
                      id="assistant-composer"
                      ref={textareaRef}
                      value={draft}
                      onChange={(event) => setDraft(event.target.value)}
                      onKeyDown={handleComposerKeyDown}
                      rows={1}
                      aria-label="发送给 AI 助手"
                      placeholder="输入你的问题、想法或指令。按 Enter 发送，Shift + Enter 换行。"
                      className={`min-h-[48px] w-full resize-none border-0 bg-transparent text-sm leading-7 focus:outline-none ${composerTextareaClass}`}
                    />
                  </div>
                </div>
              </div>
            </div>
          </main>
        </div>
      </div>
    </section>
  );
};

export default AssistantPage;
