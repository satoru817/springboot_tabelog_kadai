package com.example.demo.controller;

import com.example.demo.entity.Reservation;
import com.example.demo.entity.User;
import com.example.demo.repository.ReservationRepository;
import com.example.demo.security.UserDetailsImpl;
import com.example.demo.service.ReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/reservation")
public class ReservationController {
    private final ReservationRepository reservationRepository;
    private final ReservationService reservationService;
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy年MM月dd日 HH時mm分");

    //予約一覧画面
    @GetMapping("/show")
    public String show(@AuthenticationPrincipal UserDetailsImpl userDetails,
                       @PageableDefault(page=0,size=10,sort="date",direction= Sort.Direction.DESC) Pageable pageable,
                       Model model){
        User user = userDetails.getUser();
        Page<Reservation> reservations = reservationRepository.findAllByUser(user,pageable);
        for(Reservation reservation : reservations){
            reservation.setFormattedDate(reservation.getDate().format(formatter));
            reservation.setFormattedCreatedAt(reservation.getCreatedAt().format(formatter));
        }

        LocalDateTime currentDateTime = LocalDateTime.now();

        model.addAttribute("currentDateTime",currentDateTime);
        model.addAttribute("reservations",reservations);
        return "reservation/show";
    }
}
