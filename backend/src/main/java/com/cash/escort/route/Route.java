package com.cash.escort.route;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "route")
@Data
public class Route {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String startPoint;
    private String endPoint;
    private Integer distance;
    private String riskLevel;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
