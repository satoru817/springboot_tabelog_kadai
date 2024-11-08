package com.example.demo.service;

import com.example.demo.dto.TentativeReservationDto;
import com.example.demo.entity.Reservation;
import com.example.demo.entity.Restaurant;
import com.example.demo.repository.ReservationRepository;
import com.example.demo.repository.RestaurantRepository;
import com.example.demo.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ReservationService {
    private final ReservationRepository reservationRepository;
    private final RestaurantRepository restaurantRepository;

    public void save(Reservation reservation){
        reservationRepository.save(reservation);
    }

//    public Optional<Reservation> convertTentativeReservationDtoToReservation(@AuthenticationPrincipal UserDetailsImpl userDetails, TentativeReservationDto tentativeReservationDto){
//        Restaurant restaurant = restaurantRepository.getReferenceById(tentativeReservationDto.getRestaurantId());
//        LocalDateTime reservationDateTime = tentativeReservationDto.getReservationDateTime();
//        Integer people = tentativeReservationDto.getPeople();
//        Boolean hasFreeSlots = checkVacancy(restaurant,people,reservationDateTime);
//
//
//
//        Reservation reservation = new Reservation();
//        reservation.setDate(tentativeReservationDto.getReservationDateTime());
//        reservation.setNumberOfPeople(tentativeReservationDto.getPeople());
//        reservation.setRestaurant(restaurant);
//        reservation.setUser(userDetails.getUser());
//        return reservation;
//    }

    public Boolean checkVacancy(Restaurant restaurant,Integer people,LocalDateTime reservationDateTime){
        Integer interval = 1;
        Integer reservedCount = reservationRepository.countReservation(restaurant,reservationDateTime,reservationDateTime.minusHours(1));

        return reservedCount+people <= restaurant.getCapacity();

    }
}
