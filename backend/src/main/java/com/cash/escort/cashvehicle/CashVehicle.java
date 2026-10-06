package com.cash.escort.cashvehicle;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "cash_vehicle")
@Data
public class CashVehicle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String plate;
    private String model;
    private BigDecimal capacity;
    private String status;
    private LocalDate lastMaintain;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
