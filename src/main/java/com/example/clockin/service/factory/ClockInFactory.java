package com.example.clockin.service.factory;

import com.example.clockin.dto.ClockInEvent;
import org.springframework.stereotype.Component;

@Component
public class ClockInFactory {

    /**
     * 建立打卡事件的邏輯，可在此封裝
     * 如果只是單純 new 一個物件，也許用不到 Factory
     * 但若你要在建立物件前後做驗證或統一處理，就很適合用 Factory
     */
    public ClockInEvent createClockInEvent(String username, double lat, double lng) {
        // 例如可以先檢查 lat, lng 合理性、或加上默認時間戳
        // 這裡只是簡單示範
        return new ClockInEvent(username, lat, lng);
    }
}
