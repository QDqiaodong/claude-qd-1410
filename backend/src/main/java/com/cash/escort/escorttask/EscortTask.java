package com.cash.escort.escorttask;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "escort_task")
@Data
public class EscortTask {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String taskNo;
    private Long routeId;
    private String startSite;
    private String endSite;
    private BigDecimal amount;
    private Long memberId;
    private Long vehicleId;
    private String status;
    private LocalDate planDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
