# Stage 1: Build production React Vite bundle
FROM node:20-alpine AS build

WORKDIR /app

COPY package.json package-lock.json ./
RUN npm ci

COPY . ./
RUN npm run build

# Stage 2: Serve with lightweight Nginx Alpine
FROM nginx:alpine

# Remove default nginx configurations
RUN rm -rf /etc/nginx/conf.d/*

# Copy custom nginx reverse-proxy configuration
COPY nginx.conf /etc/nginx/conf.d/default.conf

# Copy build artifacts to nginx public root
COPY --from=build /app/dist /usr/share/nginx/html

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
