package com.eventory.common.repository;

import com.eventory.common.entity.Expo;
import com.eventory.common.entity.ExpoStatus;
import com.eventory.common.exception.CustomException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 실제 MySQL(InnoDB) 에서 동시 예약 시 정원 보장을 검증한다.
 * 결제 완료 트랜잭션과 같은 방식(락 조회 → 도메인 메서드로 인원 증가 → 커밋)을 여러 스레드가 동시에 실행한다.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Transactional(propagation = Propagation.NOT_SUPPORTED) // 각 스레드가 자기 트랜잭션을 커밋해야 하므로 테스트 트랜잭션을 쓰지 않는다
@Testcontainers
@ActiveProfiles("test")
class ExpoReservationConcurrencyTest {

    private static final int THREADS = 20;

    @Container
    @ServiceConnection
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0");

    @Autowired ExpoRepository expoRepository;
    @Autowired PlatformTransactionManager transactionManager;

    @Test
    @DisplayName("비관적 락: 마지막 1자리에 20명이 동시에 결제해도 1명만 예약된다")
    void pessimisticLock_lastSeat() throws Exception {
        Long expoId = saveExpo(100, 99);

        Result r = runConcurrently(() -> reserveWithLock(expoId));

        assertThat(r.success.get()).isEqualTo(1);
        assertThat(r.capacityExceeded.get()).isEqualTo(THREADS - 1);
        assertThat(reservedCount(expoId)).isEqualTo(100);
        print("비관적 락 / 남은 1석", r);
    }

    @Test
    @DisplayName("비관적 락: 자리가 충분하면 20건 모두 성공하고 인원이 정확히 20 증가한다 (갱신 손실 없음)")
    void pessimisticLock_noLostUpdate() throws Exception {
        Long expoId = saveExpo(100, 0);

        Result r = runConcurrently(() -> reserveWithLock(expoId));

        assertThat(r.success.get()).isEqualTo(THREADS);
        assertThat(reservedCount(expoId)).isEqualTo(THREADS);
        print("비관적 락 / 남은 100석", r);
    }

    @Test
    @DisplayName("비교: 락 없이(@Version 낙관적 락만) 동시에 예약하면 자리가 남아도 충돌로 실패하는 요청이 생긴다")
    void optimisticOnly_conflictsEvenWithSeatsLeft() throws Exception {
        Long expoId = saveExpo(100, 0);

        Result r = runConcurrently(() -> reserveWithoutLock(expoId));

        // 정합성은 @Version 이 지킨다 (성공한 만큼만 증가) — 다만 충돌한 요청은 결제 후 예약 실패가 된다
        assertThat(r.success.get() + r.optimisticConflict.get()).isEqualTo(THREADS);
        assertThat(reservedCount(expoId)).isEqualTo(r.success.get());
        print("낙관적 락만 / 남은 100석", r);
    }

    @Test
    @DisplayName("락을 오래 잡고 있으면 뒤 요청은 3초 안에 락 타임아웃으로 실패한다 (기존: JPA 힌트 무시로 최대 50초 대기)")
    void lockWaitIsBoundedTo3Seconds() throws Exception {
        Long expoId = saveExpo(100, 0);
        CountDownLatch locked = new CountDownLatch(1);
        ExecutorService pool = Executors.newSingleThreadExecutor();
        Future<?> holder = pool.submit(() -> inTransaction(tx -> {
            expoRepository.findByIdWithLock(expoId).orElseThrow(); // 행 락 획득
            locked.countDown();
            sleep(6_000);                                          // 6초 동안 락 점유
        }));
        locked.await();

        long start = System.nanoTime();
        Exception failure = null;
        try {
            reserveWithLock(expoId);
        } catch (Exception e) {
            failure = e;
        }
        long waitedMs = (System.nanoTime() - start) / 1_000_000;
        holder.get();
        pool.shutdown();
        System.out.printf("[락 타임아웃] 앞 트랜잭션 6초 점유 / 뒤 요청 대기 %dms / 결과: %s%n",
                waitedMs, failure == null ? "락 획득 후 성공" : failure.getClass().getSimpleName());

        // innodb_lock_wait_timeout = 3 → 약 3초 후 PessimisticLockingFailureException (Spring 이 MySQL 1205 오류를 변환)
        assertThat(failure).isInstanceOf(PessimisticLockingFailureException.class);
        assertThat(waitedMs).isBetween(2_500L, 5_000L);
        assertThat(reservedCount(expoId)).isZero();
    }

    private static void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    // ─── 시나리오 실행 ───

    private void reserveWithLock(Long expoId) {
        inTransaction(tx -> expoRepository.findByIdWithLock(expoId).orElseThrow().increaseReservedCount(1));
    }

    private void reserveWithoutLock(Long expoId) {
        inTransaction(tx -> expoRepository.findById(expoId).orElseThrow().increaseReservedCount(1));
    }

    private void inTransaction(Consumer<Object> work) {
        new TransactionTemplate(transactionManager).executeWithoutResult(work::accept);
    }

    private Result runConcurrently(Runnable task) throws InterruptedException {
        Result result = new Result();
        ExecutorService pool = Executors.newFixedThreadPool(THREADS);
        CountDownLatch ready = new CountDownLatch(THREADS);
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(THREADS);
        long begin = System.nanoTime();
        for (int i = 0; i < THREADS; i++) {
            pool.submit(() -> {
                ready.countDown();
                try {
                    start.await(); // 모든 스레드가 준비된 뒤 동시에 출발
                    task.run();
                    result.success.incrementAndGet();
                } catch (CustomException e) {
                    result.capacityExceeded.incrementAndGet();
                } catch (OptimisticLockingFailureException e) {
                    result.optimisticConflict.incrementAndGet();
                } catch (Exception e) {
                    result.other.incrementAndGet();
                } finally {
                    done.countDown();
                }
            });
        }
        ready.await();
        start.countDown();
        done.await(30, TimeUnit.SECONDS);
        result.elapsedMs = (System.nanoTime() - begin) / 1_000_000;
        pool.shutdown();
        return result;
    }

    // ─── fixtures ───

    private Long saveExpo(int maxCapacity, int reserved) {
        LocalDateTime now = LocalDateTime.now();
        return expoRepository.save(Expo.builder()
                .title("동시성 테스트 박람회")
                .imageUrl("/poster.svg")
                .description("desc")
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(3))
                .location("코엑스")
                .visibility(true)
                .status(ExpoStatus.APPROVED)
                .price(new BigDecimal("15000"))
                .maxCapacity(maxCapacity)
                .reservedCount(reserved)
                .displayStartDate(now)
                .displayUpdateDate(now)
                .build()).getExpoId();
    }

    private int reservedCount(Long expoId) {
        return expoRepository.findById(expoId).orElseThrow().getReservedCount();
    }

    private static void print(String label, Result r) {
        System.out.printf("[동시성] %s — 동시 %d건: 성공 %d, 정원초과 %d, 낙관적락 충돌 %d, 기타 %d, %dms%n",
                label, THREADS, r.success.get(), r.capacityExceeded.get(), r.optimisticConflict.get(), r.other.get(), r.elapsedMs);
    }

    private static class Result {
        final AtomicInteger success = new AtomicInteger();
        final AtomicInteger capacityExceeded = new AtomicInteger();
        final AtomicInteger optimisticConflict = new AtomicInteger();
        final AtomicInteger other = new AtomicInteger();
        long elapsedMs;
    }
}
