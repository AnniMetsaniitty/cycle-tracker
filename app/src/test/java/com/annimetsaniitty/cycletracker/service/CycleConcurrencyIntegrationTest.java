package com.annimetsaniitty.cycletracker.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.annimetsaniitty.cycletracker.model.User;
import com.annimetsaniitty.cycletracker.repository.CycleRepository;
import com.annimetsaniitty.cycletracker.repository.UserRepository;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class CycleConcurrencyIntegrationTest {

    @Autowired
    private CycleService cycleService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CycleRepository cycleRepository;

    @Test
    void concurrentCycleStartsLeaveOnlyOneActiveCycle() throws Exception {
        User user = userRepository.save(new User("concurrent-user", "concurrent-user@example.com", "hash"));
        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);

        ExecutorService executor = Executors.newFixedThreadPool(2);
        try {
            Future<?> first = executor.submit(() -> startCycle(user.getId(), ready, start));
            Future<?> second = executor.submit(() -> startCycle(user.getId(), ready, start));

            assertThat(ready.await(5, TimeUnit.SECONDS)).isTrue();
            start.countDown();

            first.get(5, TimeUnit.SECONDS);
            second.get(5, TimeUnit.SECONDS);
        } finally {
            executor.shutdownNow();
        }

        assertThat(cycleRepository.countByUserIdAndEndDateIsNull(user.getId())).isEqualTo(1);
    }

    private void startCycle(Long userId, CountDownLatch ready, CountDownLatch start) {
        ready.countDown();
        await(start);
        cycleService.startNewCycle(userId);
    }

    private void await(CountDownLatch latch) {
        try {
            if (!latch.await(5, TimeUnit.SECONDS)) {
                throw new IllegalStateException("Timed out waiting to start concurrent cycle test");
            }
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while waiting to start concurrent cycle test", exception);
        }
    }
}
