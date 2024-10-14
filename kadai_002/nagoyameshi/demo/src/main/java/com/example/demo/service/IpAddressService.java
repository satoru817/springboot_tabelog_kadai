package com.example.demo.service;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;

@Service
public class IpAddressService {

    public String getClientIP(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");

        if (ip != null && !ip.isEmpty()) {
            // カンマで分割して最初のIPアドレスを取得
            ip = ip.split(",")[0].trim();
        } else {
            // X-Forwarded-For ヘッダーが存在しない場合は、リモートアドレスを使用
            ip = request.getRemoteAddr();

            // もしリモートアドレスがnullまたは空なら、unknownにする。この場合アクセスを許さない
            if (ip == null || ip.isEmpty()) {
                ip = "unknown"; // または適切なデフォルト値を設定
            }
        }
        return ip;
    }
}
