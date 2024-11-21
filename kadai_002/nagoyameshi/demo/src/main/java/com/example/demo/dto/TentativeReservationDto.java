package com.example.demo.dto;

import com.example.demo.entity.Reservation;
import lombok.Data;

import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

//レストラン予約時に使うdto
@Data
public class TentativeReservationDto {
    private Integer restaurantId;
    private LocalDate date;
    private String time;
    private Integer people;
    private String comment;

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    public LocalDateTime getReservationDateTime() {
        try{
            // timeフィールドをLocalTimeに変換
            LocalTime localTime = LocalTime.parse(time, TIME_FORMATTER);
            // LocalDateとLocalTimeを結合してLocalDateTimeに変換
            return date.atTime(localTime);
        } catch (Exception e){
            return  null;
        }

    }


}
