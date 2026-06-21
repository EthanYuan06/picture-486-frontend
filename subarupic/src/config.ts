
/**
 * API Configuration
 * 
 * 采用相对路径配置：
 * 1. 开发环境：请求发送给 Vite Dev Server，由 vite.config.ts 中的 proxy 转发给后端 (localhost:8124)
 * 2. 生产环境：请求发送给当前域名，由 Nginx 或网关转发给后端
 */

// 基础路径置空，直接使用相对路径
export const API_BASE_URL = '';
export const AI_API_BASE_URL = 'http://127.0.0.1:8024';

// 集中管理所有 API 端点
export const API_ROUTES = {
  LOGIN: `/api/user/login`,
  REGISTER: `/api/user/register`,
  RESET_PASSWORD: `/api/user/reset`,
  GET_LOGIN: `/api/user/get/login`,
  LOGOUT: `/api/user/logout`,
  USER_GET_VO: `/api/user/get/vo`,
  USER_GET: `/api/user/get`,
  USER_UPDATE: `/api/user/update`,
  USER_DELETE: `/api/user/delete`,
  USER_LIST_PAGE_VO: `/api/user/list/page/vo`,
  USER_AVATAR_UPLOAD: `/api/user/avatar/upload`,

  // Picture
  PICTURE_LIST_PAGE: `/api/picture/list/page`,
  PICTURE_LIST_PAGE_VO: `/api/picture/list/page/vo`,
  PICTURE_GET: `/api/picture/get`,
  PICTURE_GET_VO: `/api/picture/get/vo`,
  PICTURE_REVIEW: `/api/picture/review`,
  PICTURE_REVIEW_BATCH: `/api/picture/review/batch`,
  PICTURE_DELETE: `/api/picture/delete`,
  PICTURE_DELETE_BATCH: `/api/picture/delete/batch`,
  PICTURE_TAG_CATEGORY: `/api/picture/tag_category`,
  PICTURE_UPLOAD: `/api/picture/upload`,
  PICTURE_UPLOAD_COVER: `/api/picture/upload/cover`,
  PICTURE_UPLOAD_BATCH: `/api/picture/upload/batch`,
  PICTURE_EDIT: `/api/picture/edit`,
  PICTURE_EDIT_BATCH: `/api/picture/edit/batch`,

  // Space
  SPACE_ADD: `/api/space/add`,
  SPACE_UPDATE: `/api/space/update`,
  SPACE_DELETE: `/api/space/delete`,
  SPACE_EDIT: `/api/space/edit`,
  SPACE_LIST_PAGE: `/api/space/list/page`,
  SPACE_LIST_PAGE_VO: `/api/space/list/page/vo`,
  SPACE_GET: `/api/space/get`,
  SPACE_GET_VO: `/api/space/get/vo`,
  SPACE_LIST_LEVEL: `/api/space/list/level`,

  // Space Analyze
  SPACE_ANALYZE_USAGE: `/api/space/analyze/usage`,
  SPACE_ANALYZE_CATEGORY: `/api/space/analyze/category`,
  SPACE_ANALYZE_TAG: `/api/space/analyze/tag`,
  SPACE_ANALYZE_SIZE: `/api/space/analyze/size`,
  SPACE_ANALYZE_USER: `/api/space/analyze/user`,

  // Space User
  SPACE_USER_ADD: `/api/spaceUser/add`,
  SPACE_USER_DELETE: `/api/spaceUser/delete`,
  SPACE_USER_GET_VO: `/api/spaceUser/get/vo`,
  SPACE_USER_LIST: `/api/spaceUser/list`,
  SPACE_USER_EDIT: `/api/spaceUser/edit`,
  SPACE_USER_LIST_ME: `/api/spaceUser/list/me`,

  // Message
  MESSAGE_SEND: `/api/message/send`,
  MESSAGE_LIST_PAGE_VO: `/api/message/list/page/vo`,
  MESSAGE_UNREAD_COUNT: `/api/message/unread/count`,
  MESSAGE_READ: `/api/message/read`,
  MESSAGE_READ_ALL: `/api/message/read/all`,

  // AI Assistant
  AI_CREATE_THREAD: `/api/create-thread`,
  AI_CHECK_THREAD: `/api/check-thread`,
  AI_DELETE_THREAD: `/api/delete-thread`,
  AI_COS_PRESIGN: `/api/cos/presign`,
  CHAT: `/api/chat`,
  CHAT_STREAM: `/api/chat/stream`,
  CHAT_STREAM_RESUME: `/api/chat/stream`,
};
