package com.cash.escort.cashvehicle;

import com.cash.escort.common.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/vehicles")
public class CashVehicleController {

    @Autowired
    private CashVehicleService service;

    @GetMapping
    public ApiResponse<List<CashVehicle>> list(@RequestParam(required = false) String status) {
        return ApiResponse.success(service.list(status));
    }

    @PostMapping
    public ApiResponse<CashVehicle> create(@RequestBody CashVehicle req) {
        return ApiResponse.success(service.create(req.getPlate(), req.getModel(),
                req.getCapacity(), req.getStatus(), req.getLastMaintain()));
    }

    @PutMapping("/{id}")
    public ApiResponse<CashVehicle> update(@PathVariable Long id, @RequestBody CashVehicle req) {
        return ApiResponse.success(service.update(id, req));
    }

    @PostMapping("/{id}/status")
    public ApiResponse<CashVehicle> transferStatus(@PathVariable Long id,
                                                  @RequestParam(required = false) String status) {
        return ApiResponse.success(service.transferStatus(id, status));
    }
}
