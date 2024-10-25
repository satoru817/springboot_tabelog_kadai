package com.example.demo.service;

import com.example.demo.entity.CompanyInfo;
import com.example.demo.repository.CompanyInfoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CompanyInfoService {
    private final CompanyInfoRepository companyInfoRepository;

    public CompanyInfo getCompanyInfo(){
        return companyInfoRepository.findFirstByOrderByCompanyInfoIdAsc();
    }
}
