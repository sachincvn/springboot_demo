package com.spring.demo.springdemo.integration.exchangerate;

import com.spring.demo.springdemo.common.currency.CurrencyCode;
import com.spring.demo.springdemo.common.exception.ExternalServiceException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

/**
 * Adapter for the free Frankfurter API (ECB rates, no API key): https://frankfurter.dev
 * All vendor details (URL shape, JSON format, errors) stay inside this class.
 */
@Slf4j
@Component
class FrankfurterExchangeRateClient implements ExchangeRateClient {

    private final RestClient restClient;

    FrankfurterExchangeRateClient(ExchangeRateProperties properties) {
        // always set timeouts on outbound calls; a slow vendor must not hang our request threads
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(properties.connectTimeout());
        requestFactory.setReadTimeout(properties.readTimeout());

        this.restClient = RestClient.builder()
                .baseUrl(properties.baseUrl())
                .requestFactory(requestFactory)
                .build();
    }

    @Override
    public BigDecimal getRate(CurrencyCode from, CurrencyCode to) {
        if (from == to) {
            return BigDecimal.ONE;
        }
        FrankfurterResponse response;
        try {
            response = restClient.get()
                    .uri(uri -> uri.path("/latest")
                            .queryParam("base", from)
                            .queryParam("symbols", to)
                            .build())
                    .retrieve()
                    .body(FrankfurterResponse.class);
        } catch (RestClientException ex) {
            throw new ExternalServiceException("Exchange-rate service is unavailable", ex);
        }

        BigDecimal rate = response == null || response.rates() == null ? null : response.rates().get(to.name());
        if (rate == null) {
            throw new ExternalServiceException("Exchange-rate service returned no %s->%s rate".formatted(from, to), null);
        }
        log.debug("Rate {}->{} = {} (date {})", from, to, rate, response.date());
        return rate;
    }

    /** Shape of the vendor's JSON. Kept private to this adapter; never leaks into our API. */
    record FrankfurterResponse(String base, LocalDate date, Map<String, BigDecimal> rates) {
    }
}
