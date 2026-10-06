package com.cash.escort.route;

import com.cash.escort.common.BizException;
import com.cash.escort.common.Dict;
import com.cash.escort.escorttask.EscortTask;
import com.cash.escort.escorttask.EscortTaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class RouteService {

    @Autowired
    private RouteRepository repository;
    @Autowired
    private EscortTaskRepository escortTaskRepository;

    public List<Route> list(String status) {
        if (status != null) {
            return repository.findByStatus(status);
        }
        return repository.findAll();
    }

    public Route create(String name, String startPoint, String endPoint, Integer distance,
                        String riskLevel, String status) {
        if (name == null) {
            throw new BizException("线路名称不能为空");
        }
        if (startPoint == null) {
            throw new BizException("起点不能为空");
        }
        if (endPoint == null) {
            throw new BizException("终点不能为空");
        }
        if (distance == null) {
            throw new BizException("里程不能为空");
        }
        String realRisk = riskLevel;
        if (realRisk == null) {
            realRisk = "低";
        }
        if (!Dict.isValidRouteRisk(realRisk)) {
            throw new BizException("非法的线路风险等级");
        }
        String realStatus = status;
        if (realStatus == null) {
            realStatus = Dict.ROUTE_ENABLED;
        }
        if (!Dict.isValidRouteStatus(realStatus)) {
            throw new BizException("非法的线路状态");
        }
        Route route = new Route();
        route.setName(name);
        route.setStartPoint(startPoint);
        route.setEndPoint(endPoint);
        route.setDistance(distance);
        route.setRiskLevel(realRisk);
        route.setStatus(realStatus);
        route.setCreatedAt(LocalDateTime.now());
        return repository.save(route);
    }

    public Route update(Long id, Route req) {
        Route route = repository.findById(id).orElse(null);
        if (route == null) {
            throw new BizException("押运线路不存在");
        }
        if (req.getName() != null) {
            route.setName(req.getName());
        }
        if (req.getStartPoint() != null) {
            route.setStartPoint(req.getStartPoint());
        }
        if (req.getEndPoint() != null) {
            route.setEndPoint(req.getEndPoint());
        }
        if (req.getDistance() != null) {
            route.setDistance(req.getDistance());
        }
        if (req.getRiskLevel() != null) {
            if (!Dict.isValidRouteRisk(req.getRiskLevel())) {
                throw new BizException("非法的线路风险等级");
            }
            route.setRiskLevel(req.getRiskLevel());
        }
        if (req.getStatus() != null) {
            if (!Dict.isValidRouteStatus(req.getStatus())) {
                throw new BizException("非法的线路状态");
            }
            route.setStatus(req.getStatus());
        }
        route.setUpdatedAt(LocalDateTime.now());
        return repository.save(route);
    }

    public Route transferStatus(Long id, String status) {
        if (status == null) {
            throw new BizException("目标状态不能为空");
        }
        if (!Dict.isValidRouteStatus(status)) {
            throw new BizException("非法的线路状态");
        }
        Route route = repository.findById(id).orElse(null);
        if (route == null) {
            throw new BizException("押运线路不存在");
        }
        if (Dict.ROUTE_DISABLED.equals(status)) {
            List<String> active = List.of(Dict.TASK_TODO, Dict.TASK_RUNNING);
            List<EscortTask> tasks = escortTaskRepository.findByRouteIdAndStatusIn(id, active);
            if (tasks != null && !tasks.isEmpty()) {
                throw new BizException("该线路仍有进行中的押运任务，不可停用");
            }
        }
        route.setStatus(status);
        route.setUpdatedAt(LocalDateTime.now());
        return repository.save(route);
    }
}
