package com.example.demo.controller;

import com.example.demo.dto.TentativeReservationDto;
import com.example.demo.entity.Category;
import com.example.demo.entity.Reservation;
import com.example.demo.entity.Restaurant;
import com.example.demo.repository.CategoryRepository;
import com.example.demo.repository.CategoryRestaurantRepository;
import com.example.demo.repository.RestaurantImageRepository;
import com.example.demo.repository.RestaurantRepository;
import com.example.demo.security.UserDetailsImpl;
import com.example.demo.service.CategoryService;
import com.example.demo.service.NagoyaService;
import com.example.demo.service.ReservationService;
import com.example.demo.service.RestaurantService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.sql.Time;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    private final ReservationService reservationService;


    @GetMapping
    public String index(@RequestParam(name="restaurantName",required=false) String restaurantName,
                        @RequestParam(name="ward",required=false) List<String> wards,//区
                        @RequestParam(name="categoryId",required=false)List<Integer> categoryIds,
                        @RequestParam(name="num",required=false) Integer num,//収容人数下限
                        @RequestParam(name="logic", required=false)String logic,
                        @PageableDefault(page=0,size=10,sort="restaurantId",direction= Sort.Direction.ASC) Pageable pageable,//自動的にspringがpageable オブジェクトを生成する。
                        Model model)
    {

        Page<Restaurant> restaurantPage  = restaurantService.findRestaurantOnCondition(restaurantName,wards,categoryIds,num,logic,pageable);

        List<Category> categories = categoryRepository.findAll();

        model.addAttribute("categories",categories);
        model.addAttribute("categoryIds",categoryIds);
        model.addAttribute("wards",wards);
        model.addAttribute("nagoyaWards", nagoyaService.getNagoyaWards());
        model.addAttribute("restaurantPage",restaurantPage);
        model.addAttribute("num",num);
        model.addAttribute("restaurantName",restaurantName);
        model.addAttribute("logic",logic);

        return "restaurant/index";
    }

    @GetMapping("/{id}")
    public String show(@PathVariable("id")Integer restaurantId,
                       Model model){
        log.info("showは呼びだされています。");
        Restaurant restaurant = restaurantRepository.getReferenceById(restaurantId);
        model.addAttribute("restaurant",restaurant);

        return "restaurant/show";
    }


    @PostMapping("/checkAvailability")
    public ResponseEntity<String> checkAvailability(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                                    @RequestBody TentativeReservationDto dto){
        log.info("dtoの中身：{}",dto);

        //Reservation reservation = reservationService.convertTentativeReservationDtoToReservation(userDetails,dto);

        Boolean isAvailable = reservationService.checkVacancy(
                restaurantRepository.getReferenceById(dto.getRestaurantId()),
                dto.getPeople(),
                dto.getReservationDateTime());
        log.info("isAvailable:{}",isAvailable);

        if(isAvailable){
            return ResponseEntity.ok("the restaurant is available");
        }else{
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("The restaurant is not available for the selected time");
        }

    }

    @PostMapping("/reservations")
    public ResponseEntity<String> createReservation(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                                                @RequestBody TentativeReservationDto reservationDto){
        log.info("ログインユーザー名:{}",userDetails.getUsername());

        try{
            reservationService.save(getReservation(userDetails,reservationDto));
            return ResponseEntity.ok("reservation was successfully made");
        }catch (Exception e){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("due to some trouble you could not make reservation. please try again");
        }

    }

    private Reservation getReservation(UserDetailsImpl userDetails,
                                       TentativeReservationDto dto){
        Reservation reservation = new Reservation();
        reservation.setRestaurant(restaurantRepository.getReferenceById(dto.getRestaurantId()));
        reservation.setUser(userDetails.getUser());
        reservation.setDate(dto.getReservationDateTime());
        reservation.setComment(dto.getComment());
        reservation.setNumberOfPeople(dto.getPeople());

        return reservation;
    }





}
