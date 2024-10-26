package com.example.demo.controller;

import com.example.demo.entity.CompanyInfo;
import com.example.demo.repository.CompanyInfoRepository;
import com.example.demo.service.CompanyInfoService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.core.Conventions;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@RequiredArgsConstructor
@Controller
@RequestMapping("/admin/company")
public class AdminCompanyController {
    private final CompanyInfoService companyInfoService;

    @GetMapping("/editCompanyInfo")
    public String editCompanyInfo(Model model){
        CompanyInfo companyInfo = companyInfoService.getCompanyInfo();
        model.addAttribute("companyInfo",companyInfo);
        return "admin/company/company_edit";
    }

    @PostMapping("/updateCompanyInfo")
    public String updateCompanyInfo(@ModelAttribute @Validated CompanyInfo companyInfo,
                                    BindingResult result,
                                    RedirectAttributes redirectAttributes){
        if(result.hasErrors()){
            redirectAttributes.addFlashAttribute("companyInfo",companyInfo);
            redirectAttributes.addFlashAttribute(BindingResult.MODEL_KEY_PREFIX
            + Conventions.getVariableName(companyInfo),result);
            return "redirect:/editCompanyInfo";
        }else{
            companyInfoService.updateCompanyInfo(companyInfo);
            // フラッシュ属性にメッセージを追加
            redirectAttributes.addFlashAttribute("successMessage", "編集成功しました");
            // リダイレクト
            return "redirect:/admin/company/editCompanyInfo"; // 編集画面へのリダイレクト
        }
    }


}
