package com.network.loadbalancer.controller;

import com.network.loadbalancer.service.ReverseProxyService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/**")
public class RequestController {

    private final ReverseProxyService reverseProxyService;

    public RequestController(ReverseProxyService reverseProxyService) {
        this.reverseProxyService = reverseProxyService;
    }

    @RequestMapping(method = {RequestMethod.GET, RequestMethod.POST})
    public ResponseEntity<?> reverseProxy(HttpServletRequest servletRequest, @RequestHeader HttpHeaders headers) throws ServletException, IOException {
        var query = StringUtils.hasText(servletRequest.getQueryString()) ?
            URLDecoder.decode(servletRequest.getQueryString(), StandardCharsets.UTF_8) : servletRequest.getQueryString();

        var url = servletRequest.getServletPath();
        url = url + (null != query ? "?" + query : "");
        var method = servletRequest.getMethod() != null ? HttpMethod.valueOf(servletRequest.getMethod()) : HttpMethod.GET;

        return reverseProxyService.forwardRequest(servletRequest, headers, url, method);
    }
}
