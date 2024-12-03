package com.example.demo.controller;

import com.example.demo.Util.UtilForString;
import com.example.demo.entity.Reservation;
import com.example.demo.entity.Review;
import com.example.demo.entity.User;
import com.example.demo.repository.ReservationRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.ReservationService;
import com.example.demo.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/admin/reservation")
@RequiredArgsConstructor
public class AdminReservationController {
    private final UserService userService;
    private final ReservationRepository reservationRepository;
    private final ReservationService reservationService;
    private final ReservationController reservationController;

    @GetMapping("/show/{id}")
    public String show(@PathVariable("id")Integer userId,
                       @RequestParam(name="searchQuery",required = false)String searchQuery,
                       @PageableDefault(page=0,size=10,sort="date",direction= Sort.Direction.DESC) Pageable pageable,
                       Model model){
        User user = userService.findById(userId);

        return reservationController.execShow(user,pageable,searchQuery,model);

    }

    @GetMapping("/index")
    public String showAll(@PageableDefault(page=0,size=10,sort="date",direction= Sort.Direction.DESC) Pageable pageable,
                          Model model,
                          @RequestParam(name="searchQuery",required = false)String searchQuery){
        Page<Reservation> reservations;
        if(UtilForString.isNullOrEmpty(searchQuery)){
            reservations = reservationService.findAll(pageable);
        }else{
            reservations = reservationService.findAllBySearchQuery(searchQuery,pageable);
        }

        model.addAttribute("currentDateTime", LocalDateTime.now());
        model.addAttribute("searchQuery",searchQuery);
        model.addAttribute("reservations",reservations);

        return "admin/reservation/index";
    }

    @GetMapping("/review/show/{id}")
    public String showEachReview(@PathVariable("id")Integer reservationId,
                                 Model model){
        Reservation reservation = reservationService.findById(reservationId);
        List<Review> reviews = List.of(reservation.getReview());
        model.addAttribute("reviews",reviews);

        return "review/showEach";
    }
}
