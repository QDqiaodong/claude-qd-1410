package com.cash.escort.escortmember;

import com.cash.escort.common.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/members")
public class EscortMemberController {

    @Autowired
    private EscortMemberService service;

    @GetMapping
    public ApiResponse<List<EscortMember>> list(@RequestParam(required = false) String status) {
        return ApiResponse.success(service.list(status));
    }

    @PostMapping
    public ApiResponse<EscortMember> create(@RequestBody EscortMember req) {
        return ApiResponse.success(service.create(req.getName(), req.getCertNo(),
                req.getLevel(), req.getStatus(), req.getJoinDate()));
    }

    @PutMapping("/{id}")
    public ApiResponse<EscortMember> update(@PathVariable Long id, @RequestBody EscortMember req) {
        return ApiResponse.success(service.update(id, req));
    }

    @PostMapping("/{id}/status")
    public ApiResponse<EscortMember> transferStatus(@PathVariable Long id,
                                                    @RequestParam(required = false) String status) {
        return ApiResponse.success(service.transferStatus(id, status));
    }
}
