package com.cash.escort.route;

import com.cash.escort.common.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/routes")
public class RouteController {

    @Autowired
    private RouteService service;

    @GetMapping
    public ApiResponse<List<Route>> list(@RequestParam(required = false) String status) {
        return ApiResponse.success(service.list(status));
    }

    @PostMapping
    public ApiResponse<Route> create(@RequestBody Route req) {
        return ApiResponse.success(service.create(req.getName(), req.getStartPoint(), req.getEndPoint(),
                req.getDistance(), req.getRiskLevel(), req.getStatus()));
    }

    @PutMapping("/{id}")
    public ApiResponse<Route> update(@PathVariable Long id, @RequestBody Route req) {
        return ApiResponse.success(service.update(id, req));
    }

    @PostMapping("/{id}/status")
    public ApiResponse<Route> transferStatus(@PathVariable Long id,
                                             @RequestParam(required = false) String status) {
        return ApiResponse.success(service.transferStatus(id, status));
    }
}
