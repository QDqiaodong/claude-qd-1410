package com.cash.escort.escorttask;

import com.cash.escort.cashvehicle.CashVehicle;
import com.cash.escort.cashvehicle.CashVehicleRepository;
import com.cash.escort.common.BizException;
import com.cash.escort.common.Dict;
import com.cash.escort.escortmember.EscortMember;
import com.cash.escort.escortmember.EscortMemberRepository;
import com.cash.escort.route.Route;
import com.cash.escort.route.RouteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class EscortTaskService {

    @Autowired
    private EscortTaskRepository repository;
    @Autowired
    private RouteRepository routeRepository;
    @Autowired
    private CashVehicleRepository vehicleRepository;
    @Autowired
    private EscortMemberRepository memberRepository;

    public List<EscortTask> list(String status, Long routeId, Long vehicleId, Long memberId) {
        if (routeId != null) {
            return repository.findByRouteId(routeId);
        }
        if (status != null) {
            return repository.findByStatus(status);
        }
        if (vehicleId != null) {
            return repository.findByVehicleId(vehicleId);
        }
        if (memberId != null) {
            return repository.findByMemberId(memberId);
        }
        return repository.findAll();
    }

    public EscortTask create(String taskNo, Long routeId, String startSite, String endSite,
                             BigDecimal amount, Long memberId, Long vehicleId, LocalDate planDate) {
        if (taskNo == null) {
            throw new BizException("任务编号不能为空");
        }
        if (routeId == null) {
            throw new BizException("押运线路不能为空");
        }
        if (startSite == null) {
            throw new BizException("起点网点不能为空");
        }
        if (endSite == null) {
            throw new BizException("终点网点不能为空");
        }
        if (amount == null) {
            throw new BizException("押运金额不能为空");
        }
        if (planDate == null) {
            throw new BizException("计划日期不能为空");
        }
        Route route = routeRepository.findById(routeId).orElse(null);
        if (route == null) {
            throw new BizException("押运线路不存在");
        }
        if (!Dict.ROUTE_ENABLED.equals(route.getStatus())) {
            throw new BizException("线路已停用，不可派发押运任务");
        }
        if (memberId != null) {
            EscortMember m = memberRepository.findById(memberId).orElse(null);
            if (m == null) {
                throw new BizException("押运员不存在");
            }
            if (!Dict.MEMBER_ONDUTY.equals(m.getStatus())) {
                throw new BizException("押运员非在岗状态，不可分配");
            }
        }
        if (vehicleId != null) {
            CashVehicle v = vehicleRepository.findById(vehicleId).orElse(null);
            if (v == null) {
                throw new BizException("运钞车不存在");
            }
            if (!Dict.VEHICLE_AVAILABLE.equals(v.getStatus())) {
                throw new BizException("运钞车非可用状态，不可分配");
            }
        }
        EscortTask task = new EscortTask();
        task.setTaskNo(taskNo);
        task.setRouteId(routeId);
        task.setStartSite(startSite);
        task.setEndSite(endSite);
        task.setAmount(amount);
        task.setPlanDate(planDate);
        task.setMemberId(memberId);
        task.setVehicleId(vehicleId);
        task.setStatus(Dict.TASK_TODO);
        task.setCreatedAt(LocalDateTime.now());
        return repository.save(task);
    }

    public EscortTask update(Long id, EscortTask req) {
        EscortTask task = repository.findById(id).orElse(null);
        if (task == null) {
            throw new BizException("押运任务不存在");
        }
        if (req.getTaskNo() != null) {
            task.setTaskNo(req.getTaskNo());
        }
        if (req.getRouteId() != null) {
            Route r = routeRepository.findById(req.getRouteId()).orElse(null);
            if (r == null) {
                throw new BizException("押运线路不存在");
            }
            if (!Dict.ROUTE_ENABLED.equals(r.getStatus())) {
                throw new BizException("线路已停用，不可改派");
            }
            task.setRouteId(req.getRouteId());
        }
        if (req.getStartSite() != null) {
            task.setStartSite(req.getStartSite());
        }
        if (req.getEndSite() != null) {
            task.setEndSite(req.getEndSite());
        }
        if (req.getAmount() != null) {
            task.setAmount(req.getAmount());
        }
        if (req.getPlanDate() != null) {
            task.setPlanDate(req.getPlanDate());
        }
        if (req.getMemberId() != null) {
            EscortMember m = memberRepository.findById(req.getMemberId()).orElse(null);
            if (m == null) {
                throw new BizException("押运员不存在");
            }
            if (!Dict.MEMBER_ONDUTY.equals(m.getStatus())) {
                throw new BizException("押运员非在岗状态，不可分配");
            }
            task.setMemberId(req.getMemberId());
        }
        if (req.getVehicleId() != null) {
            CashVehicle v = vehicleRepository.findById(req.getVehicleId()).orElse(null);
            if (v == null) {
                throw new BizException("运钞车不存在");
            }
            if (!Dict.VEHICLE_AVAILABLE.equals(v.getStatus())) {
                throw new BizException("运钞车非可用状态，不可分配");
            }
            task.setVehicleId(req.getVehicleId());
        }
        task.setUpdatedAt(LocalDateTime.now());
        return repository.save(task);
    }

    public EscortTask transferStatus(Long id, String status) {
        if (status == null) {
            throw new BizException("目标状态不能为空");
        }
        if (!Dict.isValidTaskStatus(status)) {
            throw new BizException("非法的任务状态");
        }
        EscortTask task = repository.findById(id).orElse(null);
        if (task == null) {
            throw new BizException("押运任务不存在");
        }
        String from = task.getStatus();
        if (!Dict.canTransferTask(from, status)) {
            throw new BizException("任务状态不可从「" + from + "」流转到「" + status + "」");
        }
        if (Dict.TASK_RUNNING.equals(status)) {
            if (task.getVehicleId() == null || task.getMemberId() == null) {
                throw new BizException("任务进入执行前必须先分配运钞车与押运员");
            }
            CashVehicle v = vehicleRepository.findById(task.getVehicleId()).orElse(null);
            if (v == null || !Dict.VEHICLE_AVAILABLE.equals(v.getStatus())) {
                throw new BizException("分配的运钞车不可用");
            }
            EscortMember m = memberRepository.findById(task.getMemberId()).orElse(null);
            if (m == null || !Dict.MEMBER_ONDUTY.equals(m.getStatus())) {
                throw new BizException("分配的押运员不可在岗");
            }
        }
        task.setStatus(status);
        task.setUpdatedAt(LocalDateTime.now());
        return repository.save(task);
    }
}
