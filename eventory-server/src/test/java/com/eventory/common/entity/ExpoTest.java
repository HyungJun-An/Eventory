package com.eventory.common.entity;

import com.eventory.common.exception.CustomErrorCode;
import com.eventory.common.exception.CustomException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ExpoTest {

    @Test
    @DisplayName("정원을 넘는 예약은 거부하고 인원을 바꾸지 않는다")
    void increaseBeyondCapacity() {
        Expo expo = Expo.builder().maxCapacity(10).reservedCount(9).build();

        assertThatThrownBy(() -> expo.increaseReservedCount(2))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(CustomErrorCode.EXPO_CAPACITY_EXCEEDED);
        assertThat(expo.getReservedCount()).isEqualTo(9);
    }

    @Test
    @DisplayName("정원까지는 예약할 수 있다")
    void increaseUpToCapacity() {
        Expo expo = Expo.builder().maxCapacity(10).reservedCount(9).build();

        expo.increaseReservedCount(1);

        assertThat(expo.getReservedCount()).isEqualTo(10);
    }

    @Test
    @DisplayName("환불로 인원을 줄여도 0 아래로 내려가지 않는다")
    void decreaseNotBelowZero() {
        Expo expo = Expo.builder().maxCapacity(10).reservedCount(1).build();

        expo.decreaseReservedCount(3);

        assertThat(expo.getReservedCount()).isZero();
    }
}
