package com.example.demo.controller;

import com.example.demo.entity.Reservation;
import com.example.demo.entity.User;
import com.example.demo.repository.ReservationRepository;
import com.example.demo.security.UserDetailsImpl;
import com.example.demo.service.ReservationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/reservation")
@Slf4j
public class ReservationController {
    private final ReservationRepository reservationRepository;
    private final ReservationService reservationService;


    //予約一覧画面
    @GetMapping("/show")
    public String show(@AuthenticationPrincipal UserDetailsImpl userDetails,
                       @RequestParam(name="searchQuery",required = false)String searchQuery,
                       @PageableDefault(page=0,size=10,sort="date",direction= Sort.Direction.DESC) Pageable pageable,
                       Model model){
        User user = userDetails.getUser();
        return execShow(user,pageable,searchQuery,model);
    }

    //AdminReservationControllerでも使うための共通メソッド
    public String execShow(User user,Pageable pageable,String searchQuery,Model model){
        Page<Reservation> reservations ;
        if(searchQuery!=null && !searchQuery.trim().isEmpty()){
            reservations = reservationRepository.findAllByUserAndSearchQuery(searchQuery,user,pageable);
        }else{
            reservations = reservationRepository.findAllByUser(user,pageable);
        }

        model.addAttribute("user",user);
        model.addAttribute("searchQuery",searchQuery);
        model.addAttribute("currentDateTime",LocalDateTime.now());
        model.addAttribute("reservations",reservations);
        return "reservation/show";
    }


    //予約を削除して、一覧画面に戻る
    @GetMapping("/delete/{id}")
    public String delete(@PathVariable("id")Integer reservationId,
                         RedirectAttributes redirectAttributes){
        String message;
        try{
            reservationRepository.deleteById(reservationId);
            message="Your reservation was cancelled";
        } catch (RuntimeException e) {
            log.info("予約のキャンセルに失敗しました。:{}",e.getMessage());
            message="Due to some reason cancellation was unsuccessful";
        }

        redirectAttributes.addFlashAttribute("message",message);
        return  "redirect:/reservation/show";


    }


}
