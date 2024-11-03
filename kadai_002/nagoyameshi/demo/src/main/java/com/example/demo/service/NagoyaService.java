package com.example.demo.service;

import lombok.Getter;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Getter
public class NagoyaService {
    private final List<String> nagoyaWards = List.of(
            "中区",
            "東区",
            "北区",
            "西区",
            "南区",
            "守山区",
            "緑区",
            "名東区",
            "天白区",
            "昭和区",
            "瑞穂区",
            "中村区",
            "中川区",
            "港区",
            "熱田区",
            "岡崎区"
    );


}
