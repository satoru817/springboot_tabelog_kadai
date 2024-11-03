package com.example.demo.controller;

import com.example.demo.entity.Category;
import com.example.demo.entity.Restaurant;
import com.example.demo.repository.CategoryRepository;
import com.example.demo.repository.CategoryRestaurantRepository;
import com.example.demo.repository.RestaurantImageRepository;
import com.example.demo.repository.RestaurantRepository;
import com.example.demo.service.CategoryService;
import com.example.demo.service.NagoyaService;
import com.example.demo.service.RestaurantService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/restaurant")
@Slf4j
public class RestaurantController {
    private final CategoryService categoryService;
    private final CategoryRepository categoryRepository;
    private final RestaurantService restaurantService;
    private final RestaurantRepository restaurantRepository;
    private final CategoryRestaurantRepository categoryRestaurantRepository;
    private final RestaurantImageRepository restaurantImageRepository;
    private final NagoyaService nagoyaService;

    @GetMapping
    public String index(@RequestParam(name="restaurantName",required=false) String restaurantName,
                        @RequestParam(name="ward",required=false) List<String> wards,//区
                        @RequestParam(name="categoryId",required=false)List<Integer> categoryIds,
                        @RequestParam(name="num",required=false) Integer num,//収容人数下限
                        @PageableDefault(page=0,size=10,sort="restaurantId",direction= Sort.Direction.ASC) Pageable pageable,//自動的にspringがpageable オブジェクトを生成する。
                        Model model)
    {

        Page<Restaurant> restaurantPage  = restaurantService.findRestaurantOnCondition(restaurantName,wards,categoryIds,num,pageable);

        List<Category> categories = categoryRepository.findAll();

        model.addAttribute("categories",categories);
        model.addAttribute("categoryIds",categoryIds);
        model.addAttribute("wards",wards);
        model.addAttribute("nagoyaWards", nagoyaService.getNagoyaWards());
        model.addAttribute("restaurantPage",restaurantPage);
        model.addAttribute("num",num);
        model.addAttribute("restaurantName",restaurantName);

        return "restaurant/index";
    }




}
