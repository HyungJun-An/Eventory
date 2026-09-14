package com.eventory.common.seed;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.nio.charset.StandardCharsets;

/**
 * 서버 기동 시 데모 데이터 입력 (resources/db/demo-data.sql 실행)
 * - eventory.seed.enabled=true 일 때만 빈으로 등록 (기본 off → 운영 DB 보호)
 * - expo 테이블이 비어 있을 때만 실행 → 재기동해도 중복 입력되지 않음
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "eventory.seed.enabled", havingValue = "true")
public class DemoDataRunner implements CommandLineRunner {

    private static final String SCRIPT = "db/demo-data.sql";

    private final JdbcTemplate jdbcTemplate;
    private final DataSource dataSource;

    @Override
    @Transactional // 스크립트 중간에 실패하면 전부 롤백 (반쯤 채워진 DB 방지)
    public void run(String... args) {
        Long expoCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM expo", Long.class);
        if (expoCount != null && expoCount > 0) {
            log.info("[DemoData] 박람회 데이터가 이미 있어 데모 데이터 입력을 건너뜁니다.");
            return;
        }

        ResourceDatabasePopulator populator = new ResourceDatabasePopulator(new ClassPathResource(SCRIPT));
        populator.setSqlScriptEncoding(StandardCharsets.UTF_8.name());
        populator.execute(dataSource);

        log.info("[DemoData] {} 실행 완료 — 데모 계정 비밀번호: Eventory1234! "
                + "(sysadmin / expoadmin1~4 / company01~08 / user001~060)", SCRIPT);
    }
}
