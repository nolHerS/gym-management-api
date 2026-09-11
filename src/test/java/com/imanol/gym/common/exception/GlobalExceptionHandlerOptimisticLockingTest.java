package com.imanol.gym.common.exception;

import com.imanol.gym.common.dto.ApiResponse;
import jakarta.persistence.OptimisticLockException;
import org.hibernate.StaleStateException;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerOptimisticLockingTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void objectOptimisticLockingFailureReturnsConflict() {
        assertConflict(new ObjectOptimisticLockingFailureException(
                "WorkoutPlan", 1L));
    }

    @Test
    void optimisticLockExceptionReturnsConflict() {
        assertConflict(new OptimisticLockException());
    }

    @Test
    void staleStateExceptionReturnsConflict() {
        assertConflict(new StaleStateException("stale state"));
    }

    private void assertConflict(RuntimeException exception) {
        ResponseEntity<ApiResponse<Void>> response =
                handler.handleOptimisticLockingFailure(exception);

        assertThat(response.getStatusCode().value()).isEqualTo(409);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().message())
                .isEqualTo("The resource was modified concurrently");
        assertThat(response.getBody().data()).isNull();
    }
}
