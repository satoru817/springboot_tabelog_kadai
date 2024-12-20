package com.example.demo.controller;

import com.example.demo.dto.OpeningHours;
import com.example.demo.dto.TentativeReservationDto;
import com.example.demo.entity.Category;
import com.example.demo.entity.Reservation;
import com.example.demo.entity.Restaurant;
import com.example.demo.entity.User;
import com.example.demo.repository.*;
import com.example.demo.security.UserDetailsImpl;
import com.example.demo.service.CategoryService;
import com.example.demo.service.NagoyaService;
import com.example.demo.service.ReservationService;
import com.example.demo.service.RestaurantService;
import lombok.Data;
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Controller
@RequiredArgsConstructor
@Slf4j
//todo:レストラン一覧画面でもお気に入りは確認できるようにする。
public class RestaurantController {
    private final CategoryService categoryService;
    private final CategoryRepository categoryRepository;
    private final RestaurantService restaurantService;
    private final RestaurantRepository restaurantRepository;
    private final CategoryRestaurantRepository categoryRestaurantRepository;
    private final RestaurantImageRepository restaurantImageRepository;
    private final NagoyaService nagoyaService;
    private final ReservationService reservationService;
    private final ReviewRepository reviewRepository;
    private final FavoriteRepository favoriteRepository;

    //トップページ。ログイン成功したときもここに案内
    //todo:高評価上位10,最近のレビュー上位10、お気に入り数ランキング上位10を表示する。
    @GetMapping("/")
    public String showWelcomeScreen(Model model){
        Integer misokatsuId = categoryRepository.getIdByCategoryName("味噌カツ");
        Integer hitsumabushiId = categoryRepository.getIdByCategoryName("ひつまぶし");
        Integer taiwanRamenId = categoryRepository.getIdByCategoryName("台湾ラーメン");
        Integer kishimenId = categoryRepository.getIdByCategoryName("きしめん");
        Integer tebasakiId = categoryRepository.getIdByCategoryName("手羽先");

        List<Restaurant> topRated = restaurantRepository.findTopRated(5);
        List<Restaurant> mostReviewed = restaurantRepository.findTopReviewed(5);
        List<Restaurant> mostFavorited = restaurantRepository.findTopFavorited(5);

        // モデルに全てのカテゴリーIDを追加
        model.addAttribute("misokatsuId", misokatsuId);
        model.addAttribute("hitsumabushiId", hitsumabushiId);
        model.addAttribute("taiwanRamenId", taiwanRamenId);
        model.addAttribute("kishimenId", kishimenId);
        model.addAttribute("tebasakiId", tebasakiId);
        return "restaurant/welcome";
    }

    // リクエストボディを受け取るためのクラス

    @Data
    public static class OpeningHourRequest {
        private Long restaurantId;
        private LocalDate date;
    }

    @PostMapping("/restaurant/getOpeningHour")
    public ResponseEntity<OpeningHours> getOpeningHour(@RequestBody OpeningHourRequest request){
        try{
            if(request.getRestaurantId()==null||request.getDate()==null){
                return ResponseEntity.badRequest().build();
            }

            OpeningHours openingHours = restaurantService.getOpeningHours(
                    Math.toIntExact(request.getRestaurantId()),
                    request.getDate()
            );

            return ResponseEntity.ok(openingHours);
        }catch(Exception e){
            return ResponseEntity.internalServerError().build();
        }
    }

    //お気に入り一覧画面とりあえずページネーションはつける。あと、できたらキーワード検索（カテゴリ名と店舗名と住所）をつけたい。
    @GetMapping("/restaurant/favorite")
    public String favorite(@AuthenticationPrincipal UserDetailsImpl userDetails,
                           @PageableDefault(page=0,size=10,sort="updatedAt",direction=Sort.Direction.ASC)Pageable pageable,
                           @RequestParam(name="searchQuery",required = false )String searchQuery,
                           Model model){
        User user = userDetails.getUser();
        return showFavorite(user,pageable,searchQuery,model);
    }

    public String showFavorite(User user, Pageable pageable,String searchQuery,Model model){
        Page<Restaurant> restaurantPage;
        if(searchQuery!=null && !searchQuery.trim().isEmpty()){
            restaurantPage = restaurantRepository.findAllFavoriteBySearchQueryAndUser(searchQuery,user,pageable);
        }else{
            restaurantPage = restaurantRepository.findAllFavoriteByUser(user,pageable);
        }

        model.addAttribute("user",user);
        model.addAttribute("restaurantPage",restaurantPage);
        model.addAttribute("searchQuery",searchQuery);
        return "restaurant/favorite";//この画面の作成から
    }



    @GetMapping("/restaurant")
    public String index(@AuthenticationPrincipal(errorOnInvalidType = false) UserDetailsImpl userDetails,//nullを許容するロジックを作る
                        @RequestParam(name="restaurantName",required=false) String restaurantName,
                        @RequestParam(name="ward",required=false) List<String> wards,
                        @RequestParam(name="categoryId",required=false)List<Integer> categoryIds,
                        @RequestParam(name="num",required=false) Integer num,
                        @RequestParam(name="logic", required=false)String logic,
                        @PageableDefault(page=0,size=10,sort="restaurantId",direction= Sort.Direction.ASC) Pageable pageable,
                        Model model)
    {
        Page<Restaurant> restaurantPage = restaurantService.findRestaurantOnCondition(restaurantName,wards,categoryIds,num,logic,pageable);

        // ユーザーがログインしている場合のみ、お気に入りと評価の処理を行う
        if (userDetails != null) {
            User user = userDetails.getUser();
            restaurantPage.forEach(restaurant -> {
                float averageStar = reviewRepository.getAverageStarForRestaurant(restaurant)
                        .orElse(0.0f);
                restaurant.setAverageStar(averageStar);
                restaurant.setIsFavorite(favoriteRepository.existsByUserAndRestaurant(user, restaurant));
            });
        } else {
            // 未ログインユーザーの場合は、評価のみ設定（お気に入りはfalse）
            restaurantPage.forEach(restaurant -> {
                float averageStar = reviewRepository.getAverageStarForRestaurant(restaurant)
                        .orElse(0.0f);
                restaurant.setAverageStar(averageStar);
                restaurant.setIsFavorite(false);
            });
        }

        List<Category> categories = categoryRepository.findAll();

        model.addAttribute("categories", categories);
        model.addAttribute("categoryIds", categoryIds);
        model.addAttribute("wards", wards);
        model.addAttribute("nagoyaWards", nagoyaService.getNagoyaWards());
        model.addAttribute("restaurantPage", restaurantPage);
        model.addAttribute("num", num);
        model.addAttribute("restaurantName", restaurantName);
        model.addAttribute("logic", logic);

        return "restaurant/index";
    }

    @GetMapping("/restaurant/{id}")
    public String show(@AuthenticationPrincipal(errorOnInvalidType = false)  UserDetailsImpl userDetails,
                       @PathVariable("id")Integer restaurantId,
                       Model model){

        Restaurant restaurant = restaurantRepository.getReferenceById(restaurantId);

        if(userDetails!= null){
            Boolean isFavorite = favoriteRepository.existsByUserAndRestaurant(userDetails.getUser(),restaurant);
            restaurant.setIsFavorite(isFavorite);
        }

        model.addAttribute("restaurant",restaurant);

        return "restaurant/show";
    }


    @PostMapping("/restaurant/checkAvailability")
    public ResponseEntity<String> checkAvailability(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                                    @RequestBody TentativeReservationDto dto){
        log.info("dtoの中身：{}",dto);

        //Reservation reservation = reservationService.convertTentativeReservationDtoToReservation(userDetails,dto);

        Boolean isAvailable = reservationService.checkVacancy(
                restaurantRepository.getReferenceById(dto.getRestaurantId()),
                dto.getPeople(),
                dto.getReservationDateTime());
        log.info("isAvailable:{}",isAvailable);

        if(isAvailable && checkReservationValidity(dto)){
            return ResponseEntity.ok("the restaurant is available");
        }else{
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("The restaurant is not available for the selected time");
        }

    }

    //予約を実際に作成するメソッド
    @PostMapping("/restaurant/reservations")
    public ResponseEntity<String> createReservation(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                                                @RequestBody TentativeReservationDto reservationDto){
        log.info("ログインユーザー名:{}",userDetails.getUsername());

        if(!checkReservationValidity(reservationDto)){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("reservations are not available at that time");
        }

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


    //サーバー側での予約の妥当性チェック 同じことをjsで選択肢作成のときに行っているが、念の為。
    private boolean checkReservationValidity(TentativeReservationDto dto){

        LocalDateTime when = dto.getReservationDateTime();
        LocalDateTime now = LocalDateTime.now();
        log.info("when:{},now:{}",when,now);

        if(when.isBefore(now)){
            return false;
        }

        OpeningHours op = restaurantService.getOpeningHours(dto.getRestaurantId(),dto.getDate());

        if(!op.getIsBusinessDay()){
            return false;
        }

        LocalTime reservationTime = when.toLocalTime();

        return ((!op.getOpTimeAsLocalTime().isAfter(reservationTime))&&(!op.getClTimeAsLocalTime().isBefore(reservationTime)));

    }




}
