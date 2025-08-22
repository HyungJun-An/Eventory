package com.eventory.payment.service;

import com.eventory.payment.dto.PortOneCancelRequest;
import com.eventory.payment.dto.PortOnePaymentResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

/** mapper 역할의 PortOneClient 클래스...? */
@Component
@RequiredArgsConstructor
public class PortOneClient {
    private final WebClient portOneWebClient;

    public Mono<PortOnePaymentResponse> getPayment(String paymentId) {
        return portOneWebClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/payments/{paymentId}")
                        .build(paymentId))
                .retrieve()
                .bodyToMono(PortOnePaymentResponse.class);
    }

    public Mono<Void> cancelPayment(String paymentId, PortOneCancelRequest req) {
        return portOneWebClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path("/payments/{paymentId}/cancel")
                        .build(paymentId))
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(req)
                .retrieve()
                .bodyToMono(Void.class);
    }
}
