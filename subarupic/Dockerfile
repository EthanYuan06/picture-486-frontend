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

COPY nginx/default.conf /etc/nginx/conf.d/default.conf
COPY --from=builder /app/dist /usr/share/nginx/html

EXPOSE 5173
