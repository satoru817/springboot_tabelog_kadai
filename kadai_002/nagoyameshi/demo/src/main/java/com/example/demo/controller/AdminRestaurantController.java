package com.example.demo.controller;

import com.example.demo.service.CategoryService;
import com.example.demo.service.RestaurantService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;


//todo:カテゴリの編集、削除、新規作成及び、レストランの新規作成、編集、削除ができる必要がある。
//todo:１つのレストランが複数のカテゴリを所持できるようにしているから、レストラン登録時にDBからとってきた
// 複数のカテゴリのうち任意の数を選択して、それらに対応したCategoryRestaurantを作成し保存する。

@RequiredArgsConstructor
@Controller
@RequestMapping("/admin/restaurant")
public class AdminRestaurantController {
    private final CategoryService categoryService;
    private final RestaurantService restaurantService;

    @GetMapping("/category/register")
    public String registerRestaurantCategory(Model model){
        //カテゴリは多くなりうる。したがって、ページネーションをつけたい。
        //既存のカテゴリを見て編集するページと新たなカテゴリを追加するページは同一にしたい。
        //既存のカテゴリの検索機能もつけたい。
    }
}
