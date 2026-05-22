FROM node:20-alpine AS builder

WORKDIR /app

# Install dependencies with lockfile for reproducible builds.
COPY package*.json ./
RUN npm ci

# Build static assets.
COPY . .
RUN npm run build

# Lightweight runtime image for static hosting.
FROM nginxinc/nginx-unprivileged:alpine

# 设置默认环境变量（线上云端地址）
# 部署时如果不设置环境变量，将默认请求云端后端
ENV BACKEND_API_URL=https://subaru-pic-backend-247075-4-1422702638.sh.run.tcloudbase.com/api/
ENV BACKEND_WS_URL=https://subaru-pic-backend-247075-4-1422702638.sh.run.tcloudbase.com/api/ws/

COPY nginx/default.conf /etc/nginx/templates/default.conf.template
COPY --from=builder /app/dist /usr/share/nginx/html

EXPOSE 5173
