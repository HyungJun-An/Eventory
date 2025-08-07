# 1단계: 빌드
  FROM node:20-alpine as builder
  
  WORKDIR /app
  
  COPY package*.json ./
  RUN npm install
  
  COPY . .
  RUN npm run build
  
  # 2단계: nginx 이미지
  FROM nginx:stable-alpine
  
  # nginx 설정 복사
  COPY default.conf /etc/nginx/conf.d/default.conf
  
  # 빌드된 정적 파일 복사
  COPY --from=builder /app/dist /usr/share/nginx/html
  
  # SSL 및 권한
  COPY ssl/certificate.crt /etc/ssl/certificate.crt
  COPY ssl/private.key /etc/ssl/private.key
  RUN chmod -R 755 /usr/share/nginx/html
  
  EXPOSE 80 443
  
  CMD ["nginx", "-g", "daemon off;"]