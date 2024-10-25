package com.example.demo.controller;

import com.example.demo.entity.CompanyInfo;
import com.example.demo.service.CompanyInfoService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@RequiredArgsConstructor
@Controller
public class AdminCompanyController {
    private final CompanyInfoService companyInfoService;

    @GetMapping("/editCompanyInfo")
    public String editCompanyInfo(Model model){
        CompanyInfo companyInfo = companyInfoService.getCompanyInfo();
        model.addAttribute("companyInfo",companyInfo);
        return "admin/company/company_edit";
    }
}
