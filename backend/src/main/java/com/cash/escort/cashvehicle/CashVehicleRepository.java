package com.cash.escort.cashvehicle;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CashVehicleRepository extends JpaRepository<CashVehicle, Long> {

    List<CashVehicle> findByStatus(String status);
}
