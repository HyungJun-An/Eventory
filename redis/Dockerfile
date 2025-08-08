FROM redis:7.0.11

# 커스텀 redis.conf 복사
COPY redis.conf /usr/local/etc/redis/redis.conf

CMD ["redis-server", "/usr/local/etc/redis/redis.conf"]
