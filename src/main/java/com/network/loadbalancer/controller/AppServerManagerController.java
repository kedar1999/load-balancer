package com.network.loadbalancer.controller;

import com.network.loadbalancer.model.AppServerDetail;
import com.network.loadbalancer.service.AppServerManager;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/app-server")
public class AppServerManagerController {

    @PostMapping("/register")
    public ResponseEntity<AppServerDetail> registerAppServer(@RequestBody AppServerDetail appServerDetail) throws Exception {
        return ResponseEntity.ok(AppServerManager.registerAppServer(appServerDetail));
    }

    @DeleteMapping("/remove")
    public ResponseEntity<AppServerDetail> deRegisterAppServer(@RequestBody AppServerDetail appServerDetail) throws Exception {
        return ResponseEntity.ok(AppServerManager.removeAppServer(appServerDetail));
    }

    @GetMapping
    public ResponseEntity<List<AppServerDetail>> getAppServers() {
        return ResponseEntity.ok(AppServerManager.getAllAppServers());
    }
}
