package com.example.demo.dto;

import lombok.Data;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

@Data
public class OpeningHours {
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    private Boolean isBusinessDay;
    private String openingTime;  // "HH:mm" 形式
    private String closingTime; // "HH:mm" 形式

    public LocalTime getOpTimeAsLocalTime(){
        if(!isBusinessDay||openingTime==null){
            return null;
        }

        return LocalTime.parse(openingTime,TIME_FORMATTER);
    }

    public LocalTime getClTimeAsLocalTime(){
        if(!isBusinessDay||closingTime==null){
            return null;
        }

        return LocalTime.parse(closingTime,TIME_FORMATTER);
    }

}
