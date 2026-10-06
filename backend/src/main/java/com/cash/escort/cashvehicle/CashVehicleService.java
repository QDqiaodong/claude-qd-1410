package com.cash.escort.cashvehicle;

import com.cash.escort.common.BizException;
import com.cash.escort.common.Dict;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class CashVehicleService {

    @Autowired
    private CashVehicleRepository repository;

    public List<CashVehicle> list(String status) {
        if (status != null) {
            return repository.findByStatus(status);
        }
        return repository.findAll();
    }

    public CashVehicle create(String plate, String model, BigDecimal capacity, String status, LocalDate lastMaintain) {
        if (plate == null) {
            throw new BizException("车牌不能为空");
        }
        String realStatus = status;
        if (realStatus == null) {
            realStatus = Dict.VEHICLE_AVAILABLE;
        }
        if (!Dict.isValidVehicleStatus(realStatus)) {
            throw new BizException("非法的车辆状态");
        }
        CashVehicle vehicle = new CashVehicle();
        vehicle.setPlate(plate);
        vehicle.setModel(model);
        vehicle.setCapacity(capacity);
        vehicle.setStatus(realStatus);
        vehicle.setLastMaintain(lastMaintain);
        vehicle.setCreatedAt(LocalDateTime.now());
        return repository.save(vehicle);
    }

    public CashVehicle update(Long id, CashVehicle req) {
        CashVehicle vehicle = repository.findById(id).orElse(null);
        if (vehicle == null) {
            throw new BizException("运钞车不存在");
        }
        if (req.getPlate() != null) {
            vehicle.setPlate(req.getPlate());
        }
        if (req.getModel() != null) {
            vehicle.setModel(req.getModel());
        }
        if (req.getCapacity() != null) {
            vehicle.setCapacity(req.getCapacity());
        }
        if (req.getStatus() != null) {
            if (!Dict.isValidVehicleStatus(req.getStatus())) {
                throw new BizException("非法的车辆状态");
            }
            vehicle.setStatus(req.getStatus());
        }
        if (req.getLastMaintain() != null) {
            vehicle.setLastMaintain(req.getLastMaintain());
        }
        vehicle.setUpdatedAt(LocalDateTime.now());
        return repository.save(vehicle);
    }

    public CashVehicle transferStatus(Long id, String status) {
        if (status == null) {
            throw new BizException("目标状态不能为空");
        }
        if (!Dict.isValidVehicleStatus(status)) {
            throw new BizException("非法的车辆状态");
        }
        CashVehicle vehicle = repository.findById(id).orElse(null);
        if (vehicle == null) {
            throw new BizException("运钞车不存在");
        }
        if (Dict.VEHICLE_UNAVAILABLE.contains(vehicle.getStatus())
                && Dict.VEHICLE_AVAILABLE.equals(status)) {
            // 从维修/停用恢复可用：允许，无需额外校验
        }
        vehicle.setStatus(status);
        vehicle.setUpdatedAt(LocalDateTime.now());
        return repository.save(vehicle);
    }
}
