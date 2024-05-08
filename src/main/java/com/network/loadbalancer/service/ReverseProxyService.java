package com.network.loadbalancer.service;

import com.network.loadbalancer.config.LoadBalancerConfig;
import com.network.loadbalancer.service.request.forward.algorithm.RequestForwardDecider;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.Part;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.apache.commons.io.IOUtils;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class ReverseProxyService {

    private final LoadBalancerConfig loadBalancerConfig;
    private final RestHttpService restHttpService;

    public ReverseProxyService(LoadBalancerConfig loadBalancerConfig, RestHttpService restHttpService) {
        this.loadBalancerConfig = loadBalancerConfig;
        this.restHttpService = restHttpService;
    }

    private String getServerUrl() {
        String algorithmStr = loadBalancerConfig.getAlgorithm();
        RequestForwardDecider.Algorithm algorithm = RequestForwardDecider.Algorithm.valueOf(algorithmStr);
        return RequestForwardDecider.getAppServerUrl(algorithm);
    }

    public ResponseEntity<?> forwardRequest(HttpServletRequest servletRequest, HttpHeaders headers, String url, HttpMethod method) throws ServletException, IOException {
        String appServerUrl = getServerUrl();

        //reverse proxy the request to app server
        ResponseEntity<String> responseEntity = null;

        try {
            responseEntity = restHttpService.makeActualHttpCall(appServerUrl, getBody(servletRequest), url, headers, method);
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.UNAUTHORIZED || e.getStatusCode() == HttpStatus.FORBIDDEN) {
                return ResponseEntity.status(e.getStatusCode()).build();
            }
            return ResponseEntity.status(e.getStatusCode()).headers(e.getResponseHeaders()).body(e.getResponseBodyAsString());
        } catch (Exception e) {
            throw e;
        }
        return responseEntity;
    }

    private Object getBody(HttpServletRequest request) throws IOException, ServletException {
        if (isMultiPartRequest(request)) {
            Map<String, List<HttpEntity>> map = request.getParts().stream()
                    .collect(Collectors.groupingBy(jakarta.servlet.http.Part::getName))
                    .entrySet().stream()
                    .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().stream()
                            .map(part -> {
                                try {
                                    Map<String, List<String>> headers = getHeaders(part);
                                    if (headers.get("content-type") == null || Objects.requireNonNull(headers.get("content-type")).isEmpty())
                                        headers.put("content-type", List.of("text/plain"));
                                    return new HttpEntity(IOUtils.toByteArray(part.getInputStream()), (MultiValueMap<String, String>) headers);
                                } catch (IOException ex) {
                                    throw new RuntimeException(ex);
                                }
                            })
                            .collect(Collectors.toList())));

            return new LinkedMultiValueMap<>(map);
        } else {
            return IOUtils.toByteArray(request.getReader(), StandardCharsets.UTF_8);
        }
    }

    private HttpHeaders getHeaders(Part part) {
        var headers = new HttpHeaders();
        for (String it : part.getHeaderNames()) {
            headers.addAll(it, part.getHeaders(it).stream().toList());
        }
        return headers;
    }

    private boolean isMultiPartRequest(HttpServletRequest request) {
        String contentType = request.getContentType();
        return contentType != null && contentType.toLowerCase().startsWith("multipart/");
    }
}
