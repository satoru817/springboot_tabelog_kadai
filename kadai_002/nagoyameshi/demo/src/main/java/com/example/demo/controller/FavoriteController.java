package com.example.demo.controller;

import com.example.demo.entity.Favorite;
import com.example.demo.entity.Restaurant;
import com.example.demo.entity.User;
import com.example.demo.repository.FavoriteRepository;
import com.example.demo.repository.RestaurantRepository;
import com.example.demo.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
@RequestMapping("/favorite")
public class FavoriteController {
    private final FavoriteRepository favoriteRepository;
    private final RestaurantRepository restaurantRepository;

    @Transactional//deleteにはこの設定が必要。saveは不要
    @GetMapping("/toggle/{id}/{isFavorite}")
    public String toggleFavorite(
            @PathVariable(name="id")Integer restaurantId,
            @PathVariable(name="isFavorite") String isFavorite,
            @AuthenticationPrincipal UserDetailsImpl userDetailsImpl,
            Model model,
            RedirectAttributes redirectAttributes) {

        Restaurant restaurant = restaurantRepository.getReferenceById(restaurantId);
        User user = userDetailsImpl.getUser();

        if(isFavorite.equals("true")){
            favoriteRepository.deleteByUserAndRestaurant(user,restaurant);
        }else{
            Favorite favorite = new Favorite();
            favorite.setUser(user);
            favorite.setRestaurant(restaurant);
            favoriteRepository.save(favorite);
        }

        return "redirect:/restaurant/"+restaurantId;
    }
}
