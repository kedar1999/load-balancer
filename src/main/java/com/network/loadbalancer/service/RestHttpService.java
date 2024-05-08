package com.network.loadbalancer.service;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClient.RequestBodySpec;
import org.springframework.web.reactive.function.client.WebClient.ResponseSpec;
import reactor.netty.http.client.HttpClient;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Service
public class RestHttpService {

    private final Set<String> blackListedHeaders = new HashSet<>();

    public RestHttpService() {
        blackListedHeaders.add("host");
        blackListedHeaders.add("referer");
        blackListedHeaders.add("accept-encoding");
    }

    public <I> ResponseEntity<String> makeActualHttpCall(String appServerBaseUrl, I input, String endpoint, HttpHeaders headers, HttpMethod verb) {
        RequestBodySpec requestBodySpec = getWebClient(appServerBaseUrl)
                .method(verb)
                .uri(endpoint)
                .headers(header -> header.addAll(getHeaders(headers)));

        ResponseSpec responseSpec = input != null ?
                requestBodySpec.body(BodyInserters.fromValue(input)).retrieve() :
                requestBodySpec.retrieve();

        return responseSpec.toEntity(String.class).block();
    }

    public WebClient getWebClient(String baseUrl) {

        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.SO_KEEPALIVE, true)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, (int) 10000)
                .doOnConnected(conn -> conn.addHandlerLast(new ReadTimeoutHandler(10000, TimeUnit.MILLISECONDS)));

        ReactorClientHttpConnector httpConnector = new ReactorClientHttpConnector(httpClient);

        return WebClient.builder()
                //.filter(new RestAuthHeaderFilter(proxyServiceConfig.getAuth()))
                .baseUrl(baseUrl)
                .clientConnector(httpConnector)
                .build();
    }

    private HttpHeaders getHeaders(HttpHeaders httpHeaders) {
        HttpHeaders headers = new HttpHeaders();
        if (httpHeaders != null) {
            httpHeaders.forEach((key, value) -> {
                if (!blackListedHeaders.contains(key.toLowerCase())) {
                    headers.addAll(key, value);
                }
            });
        }
        return headers;
    }
}

