package com.example.demo.repository;

import com.example.demo.entity.CompanyInfo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CompanyInfoRepository extends JpaRepository<CompanyInfo,Integer> {
    CompanyInfo findFirstByOrderByCompanyInfoIdAsc();
}
