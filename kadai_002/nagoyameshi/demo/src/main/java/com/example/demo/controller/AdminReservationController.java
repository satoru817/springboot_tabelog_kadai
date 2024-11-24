package com.example.demo.controller;

import com.example.demo.entity.User;
import com.example.demo.repository.ReservationRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.ReservationService;
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
@RequestMapping("/admin/reservation")
@RequiredArgsConstructor
public class AdminReservationController {
    private final UserRepository userRepository;
    private final ReservationRepository reservationRepository;
    private final ReservationService reservationService;
    private final ReservationController reservationController;

    @GetMapping("/show/{id}")
    public String show(@PathVariable("id")Integer userId,
                       @RequestParam(name="searchQuery",required = false)String searchQuery,
                       @PageableDefault(page=0,size=10,sort="date",direction= Sort.Direction.DESC) Pageable pageable,
                       Model model){
        User user = userRepository.getReferenceById(userId);

        return reservationController.execShow(user,pageable,searchQuery,model);

    }
}
