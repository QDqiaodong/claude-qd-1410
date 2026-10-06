package com.cash.escort.common;

import java.util.List;
import java.util.Map;

/**
 * 金融押运公司业务词典与规则参数表：
 * 押运任务状态流转、运钞车状态、押运员状态、押运线路状态/风险等级。
 * 规则集中在此，便于出题时被要求调整或扩展。
 */
public final class Dict {

    private Dict() {
    }

    // ---------------- 押运任务 escort_task ----------------
    public static final String TASK_TODO = "待派车";
    public static final String TASK_RUNNING = "执行中";
    public static final String TASK_DONE = "已完成";
    public static final String TASK_ABNORMAL = "异常";
    public static final List<String> TASK_STATUS = List.of(TASK_TODO, TASK_RUNNING, TASK_DONE, TASK_ABNORMAL);

    /** 任务状态机：待派车→执行中/异常；执行中→已完成/异常；异常→待派车；完成为终态 */
    public static final Map<String, List<String>> TASK_FLOW = Map.of(
            TASK_TODO, List.of(TASK_RUNNING, TASK_ABNORMAL),
            TASK_RUNNING, List.of(TASK_DONE, TASK_ABNORMAL),
            TASK_ABNORMAL, List.of(TASK_TODO),
            TASK_DONE, List.of());

    // ---------------- 运钞车 cash_vehicle ----------------
    public static final String VEHICLE_AVAILABLE = "可用";
    public static final String VEHICLE_REPAIR = "维修";
    public static final String VEHICLE_DISABLED = "停用";
    public static final List<String> VEHICLE_STATUS = List.of(VEHICLE_AVAILABLE, VEHICLE_REPAIR, VEHICLE_DISABLED);
    /** 不可被押运任务分配的的车辆状态 */
    public static final List<String> VEHICLE_UNAVAILABLE = List.of(VEHICLE_REPAIR, VEHICLE_DISABLED);

    // ---------------- 押运员 escort_member ----------------
    public static final String MEMBER_ONDUTY = "在岗";
    public static final String MEMBER_REST = "休班";
    public static final String MEMBER_DISABLED = "停用";
    public static final List<String> MEMBER_STATUS = List.of(MEMBER_ONDUTY, MEMBER_REST, MEMBER_DISABLED);
    /** 不可被押运任务分配的押运员状态 */
    public static final List<String> MEMBER_UNAVAILABLE = List.of(MEMBER_REST, MEMBER_DISABLED);

    // ---------------- 押运线路 route ----------------
    public static final String ROUTE_ENABLED = "启用";
    public static final String ROUTE_DISABLED = "停用";
    public static final List<String> ROUTE_STATUS = List.of(ROUTE_ENABLED, ROUTE_DISABLED);
    public static final List<String> ROUTE_RISK = List.of("低", "中", "高");

    // ---------------- 取值/校验方法 ----------------

    public static boolean isValidTaskStatus(String s) {
        return s != null && TASK_STATUS.contains(s);
    }

    public static boolean isValidVehicleStatus(String s) {
        return s != null && VEHICLE_STATUS.contains(s);
    }

    public static boolean isValidMemberStatus(String s) {
        return s != null && MEMBER_STATUS.contains(s);
    }

    public static boolean isValidRouteStatus(String s) {
        return s != null && ROUTE_STATUS.contains(s);
    }

    public static boolean isValidRouteRisk(String s) {
        return s != null && ROUTE_RISK.contains(s);
    }

    /**
     * 押运任务状态流转校验：完成为终态；异常仅可回退到待派车。
     */
    public static boolean canTransferTask(String from, String to) {
        if (!isValidTaskStatus(from) || !isValidTaskStatus(to)) {
            return false;
        }
        List<String> next = TASK_FLOW.get(from);
        return next != null && next.contains(to);
    }
}
