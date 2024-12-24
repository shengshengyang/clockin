package com.example.clockin.service.facade;

import com.example.clockin.dto.ClockInEvent;
import com.example.clockin.dto.ClockInRequest;
import com.example.clockin.model.AttendanceRecord;

import com.example.clockin.repo.AttendanceRecordRepository;
import com.example.clockin.service.AttendanceService;
import com.example.clockin.service.RecordQueryService;
import com.example.clockin.service.factory.ClockInFactory;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@Service
public class AttendanceFacade {

    private final AttendanceService attendanceService;
    private final RecordQueryService recordQueryService;
    private final AttendanceRecordRepository attendanceRecordRepository;
    private final ClockInFactory clockInFactory;

    public AttendanceFacade(AttendanceService attendanceService,
                            RecordQueryService recordQueryService,
                            AttendanceRecordRepository attendanceRecordRepository,
                            ClockInFactory clockInFactory) {
        this.attendanceService = attendanceService;
        this.recordQueryService = recordQueryService;
        this.attendanceRecordRepository = attendanceRecordRepository;
        this.clockInFactory = clockInFactory;
    }

    /**
     * 執行打卡 (包含複雜邏輯，如透過 Kafka / 非同步操作…)
     */
    public String clockIn(ClockInRequest request, Principal principal) throws Exception {
        // 1. 從 request 建立打卡事件(可用 factory 來產生)
        ClockInEvent event = clockInFactory.createClockInEvent(principal.getName(),
                request.getLatitude(),
                request.getLongitude());

        // 2. 呼叫 AttendanceService 去做打卡流程 (如 Kafka request-reply)
        String resultMessage = attendanceService.processClockIn(event);

        return resultMessage;
    }

    /**
     * 查詢考勤記錄
     * 回傳格式可自行決定回傳什麼資料，或直接回傳 DTO
     */
    public List<Map<String, Object>> getAttendanceRecords(Principal principal, boolean isAdmin) {
        return recordQueryService.getRecords(principal.getName(), isAdmin);
    }

    /**
     * 如果需要直接存取 DB，Facade 也可以整合 Repo
     */
    public AttendanceRecord findRecordById(Integer id) {
        return attendanceRecordRepository.findById(id).orElse(null);
    }

    // 也可以加更多方法，如 "請假"、"加班" 等等
}
