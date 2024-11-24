package com.example.demo.controller;

import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/favorite")
public class AdminFavoriteController {
    private final RestaurantController restaurantController;
    private final UserRepository userRepository;

    @GetMapping("/show/{id}")
    public String show(@PathVariable("id")Integer userId,
                       @RequestParam(name="searchQuery",required = false)String searchQuery,
                       @PageableDefault(page=0,size=10,sort="updatedAt",direction= Sort.Direction.ASC) Pageable pageable,
                       Model model){
        User user = userRepository.getReferenceById(userId);
        return restaurantController.showFavorite(user,pageable,searchQuery,model);
    }
}
