# Build-Image
FROM node:20 AS build
WORKDIR /app
COPY . .
RUN npm install
RUN npm run build

# Production-Image
FROM nginx:alpine
COPY --from=build /app/build /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
