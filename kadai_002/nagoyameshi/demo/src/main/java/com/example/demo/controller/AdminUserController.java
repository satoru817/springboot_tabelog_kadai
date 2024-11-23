package com.example.demo.controller;

import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@RequiredArgsConstructor
@Controller
@Slf4j
@RequestMapping("/admin/user")
public class AdminUserController {
    private  final UserService userService;
    private final UserRepository userRepository;

    //検索時にはnameForReservationとcreatedAtでsortする。
    @GetMapping("/")
    public String index(Model model,
                        @RequestParam(name="keyword",required = false)String keyword,
                        @PageableDefault(page=0,size=10,sort={"nameForReservation","createdAt"},direction = Sort.Direction.ASC) Pageable pageable){
        Page<User> users;
        if(keyword!=null && !keyword.trim().isEmpty()){
            users = userRepository.findAllByKeyWord(keyword,pageable);
        }else{
            users = userRepository.findAll(pageable);
        }

        model.addAttribute("users",users);
        model.addAttribute("keyword",keyword);
        return "admin/user/index";
    }

    @GetMapping("/show/{id}")
    public String show(@PathVariable("id")Integer userId,
                       Model model){
        User user = userRepository.getReferenceById(userId);
        model.addAttribute("user",user);

        return "admin/user/show";
    }

}
