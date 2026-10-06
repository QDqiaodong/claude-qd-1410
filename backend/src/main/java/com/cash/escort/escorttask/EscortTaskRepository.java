package com.cash.escort.escorttask;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EscortTaskRepository extends JpaRepository<EscortTask, Long> {

    List<EscortTask> findByRouteId(Long routeId);

    List<EscortTask> findByStatus(String status);

    List<EscortTask> findByVehicleId(Long vehicleId);

    List<EscortTask> findByMemberId(Long memberId);

    List<EscortTask> findByRouteIdAndStatusIn(Long routeId, List<String> statuses);
}
