package com.example.demo.controller;

import com.example.demo.entity.Favorite;
import com.example.demo.entity.Restaurant;
import com.example.demo.entity.User;
import com.example.demo.repository.FavoriteRepository;
import com.example.demo.repository.RestaurantRepository;
import com.example.demo.security.UserDetailsImpl;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@RequestMapping("/favorite")
@Slf4j
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

    @PostMapping("/api/toggle")
    @ResponseBody
    @Transactional
    public ResponseEntity<?> toggleFavorite(@RequestBody Map<String,String>request,
                                               @AuthenticationPrincipal UserDetailsImpl userDetails){
        try{
            User user = userDetails.getUser();
            Integer restaurantId = Integer.valueOf(request.get("restaurantId"));
            Restaurant restaurant = restaurantRepository.getReferenceById(restaurantId);
            boolean isFavorite = Boolean.parseBoolean(request.get("isFavorite"));
            Map<String,Object> response = new HashMap<>();
            log.info("isFavorite:{}",isFavorite);
            log.info("restaurant:{}",restaurant.getName());

            if(isFavorite){
                log.info("deleteしようとしています");
                favoriteRepository.deleteByUserAndRestaurant(user,restaurant);
                response.put("success",true);
                response.put("isFavorite",false);
            }else{
                createFavorite(user,restaurant);
                response.put("success",true);
                response.put("isFavorite",true);
            }

            return ResponseEntity.ok(response);
        }catch(NumberFormatException e){
            return ResponseEntity.badRequest()
                    .body(Map.of("error","Invalid restaurant ID format"));
        }catch(EntityNotFoundException e){
            return ResponseEntity.notFound()
                    .build();
        }catch(Exception e){
            return ResponseEntity.internalServerError()
                    .body(Map.of("error","Internal server error occurred"));
        }


    }

    private void createFavorite(User user, Restaurant restaurant){
        Favorite favorite = new Favorite();
        favorite.setUser(user);
        favorite.setRestaurant(restaurant);
        favoriteRepository.save(favorite);
    }
}
