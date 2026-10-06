package com.cash.escort.escorttask;

import com.cash.escort.common.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/escort-tasks")
public class EscortTaskController {

    @Autowired
    private EscortTaskService service;

    @GetMapping
    public ApiResponse<List<EscortTask>> list(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long routeId,
            @RequestParam(required = false) Long vehicleId,
            @RequestParam(required = false) Long memberId) {
        return ApiResponse.success(service.list(status, routeId, vehicleId, memberId));
    }

    @PostMapping
    public ApiResponse<EscortTask> create(@RequestBody EscortTask req) {
        return ApiResponse.success(service.create(req.getTaskNo(), req.getRouteId(), req.getStartSite(),
                req.getEndSite(), req.getAmount(), req.getMemberId(), req.getVehicleId(), req.getPlanDate()));
    }

    @PutMapping("/{id}")
    public ApiResponse<EscortTask> update(@PathVariable Long id, @RequestBody EscortTask req) {
        return ApiResponse.success(service.update(id, req));
    }

    @PostMapping("/{id}/status")
    public ApiResponse<EscortTask> transferStatus(@PathVariable Long id,
                                                 @RequestParam(required = false) String status) {
        return ApiResponse.success(service.transferStatus(id, status));
    }
}
