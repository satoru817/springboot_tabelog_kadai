package com.example.demo.controller;

import com.example.demo.entity.CompanyInfo;
import com.example.demo.service.CompanyInfoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@RequiredArgsConstructor
@Controller
public class CompanyController {
    private final CompanyInfoService companyInfoService;
    @GetMapping("/CompanyInfo")
    public String showCompanyInfo(Model model){
        CompanyInfo companyInfo = companyInfoService.getCompanyInfo();
        model.addAttribute("companyInfo",companyInfo);
        return "company/show_company_info";
    }
}
