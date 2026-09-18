# ---------- Etapa 1: compilación ----------
FROM node:22-alpine AS build
WORKDIR /app

COPY package.json package-lock.json ./
RUN npm ci

COPY . .
RUN npm run build

# ---------- Etapa 2: servidor estático ----------
FROM nginx:alpine
COPY nginx.conf /etc/nginx/conf.d/default.conf
# Angular 22 deja los estáticos en dist/<proyecto>/browser
COPY --from=build /app/dist/frontend/browser /usr/share/nginx/html

EXPOSE 80
