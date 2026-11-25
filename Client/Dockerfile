# syntax=docker/dockerfile:1
# Build context: Client/ (docker build -t cinegold-web --build-arg VITE_API_URL=https://api.example.com Client)

FROM node:24-alpine AS build
WORKDIR /app
COPY package.json package-lock.json ./
RUN npm ci --no-audit --no-fund
COPY . .
ARG VITE_API_URL=""
ENV VITE_API_URL=${VITE_API_URL}
RUN npm run build

# Unprivileged nginx (runs as uid 101, listens on 8080)
FROM nginxinc/nginx-unprivileged:1.29-alpine
ENV API_ORIGIN=""
COPY nginx.conf.template /etc/nginx/templates/default.conf.template
COPY --from=build /app/dist /usr/share/nginx/html
EXPOSE 8080
HEALTHCHECK --interval=30s --timeout=3s CMD wget -qO- http://127.0.0.1:8080/ >/dev/null || exit 1
