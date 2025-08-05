# nginx 경량 안정판
FROM nginx:stable-alpine

# 사용자 설정 nginx 컨피그로 교체
COPY default.conf /etc/nginx/conf.d/default.conf

# 빌드된 정적 파일 복사 (default.conf에서 root가 /usr/share/nginx/html로 되어 있다고 가정)
COPY ./dist /usr/share/nginx/html

# 퍼미션 정리 (대부분 필요 없지만 안전하게)
RUN chmod -R 755 /usr/share/nginx/html

EXPOSE 80 443

CMD ["nginx", "-g", "daemon off;"]
