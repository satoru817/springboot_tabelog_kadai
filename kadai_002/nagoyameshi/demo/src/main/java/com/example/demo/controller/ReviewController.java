package com.example.demo.controller;

import com.cybozu.labs.langdetect.LangDetectException;
import com.example.demo.entity.Reservation;
import com.example.demo.entity.Review;
import com.example.demo.entity.ReviewPhoto;
import com.example.demo.repository.ReservationRepository;
import com.example.demo.repository.ReviewPhotoRepository;
import com.example.demo.repository.ReviewRepository;
import com.example.demo.service.ImageService;
import com.example.demo.service.ReviewContentChecker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

@Controller
@RequiredArgsConstructor
@RequestMapping("/review")
@Slf4j
public class ReviewController {
    private final ReviewRepository reviewRepository;
    private final ReviewPhotoRepository reviewPhotoRepository;
    private final ImageService imageService;
    private final ReservationRepository reservationRepository;
    private final ReviewContentChecker reviewContentChecker;

    //todo:今のままでは他人のレビューを簡単に編集できてしまう。
    @GetMapping("/create/{id}")
    public String createReview(@PathVariable("id")Integer reservationId,
                               Model model){
        Reservation reservation = reservationRepository.getReferenceById(reservationId);
        Optional<Review> optionalReview = reviewRepository.findByReservation(reservation);
        Review review = new Review();
        if(optionalReview.isPresent()){
            review = optionalReview.get();
            review.setReservationId(reservationId);
        }else{
            review.setReservation(reservation);
            review.setReservationId(reservationId);
        }

        model.addAttribute("review",review);

        return "review/create";
    }

    //コンテントのfilteringをする
    // 合格した場合のみ登録する。そうでないときは保存しない。
    @Transactional
    @PostMapping("/upsert")
    public String upsertReview(@ModelAttribute Review review,
                               RedirectAttributes redirectAttributes) {
        String content = review.getContent();
        log.info("content:{}",content);
        boolean containsBannedWords = reviewContentChecker.containsBannedWords(content);
        if(containsBannedWords){
            String message = "Your message cannot be posted for some reason";
            redirectAttributes.addFlashAttribute("message",message);
            return "redirect:/review/create"+review.getReservation().getReservationId();
        }else{
            review.setReservation(reservationRepository.getReferenceById(review.getReservationId()));
            reviewRepository.save(review);
            imageService.saveImageOfReview(review,reviewPhotoRepository);

            String message = "Your review was posted!";
            redirectAttributes.addFlashAttribute("message",message);
            return "redirect:/reservation/show";
        }

    }


    @GetMapping("/deleteImage/{id}")
    public String deleteImage(@PathVariable("id")Integer reviewPhotoId){
        ReviewPhoto reviewPhoto = reviewPhotoRepository.getReferenceById(reviewPhotoId);
        boolean isDeleted = imageService.deleteImage(reviewPhoto.getImageName());
        if(isDeleted){
            log.info("画像は正常に削除されました。");
        }else{
            log.info("画像の削除に失敗しました。");
        }
        Reservation reservation = reviewPhoto.getReview().getReservation();
        reviewPhotoRepository.delete(reviewPhoto);

        return "redirect:/review/create/"+reservation.getReservationId();
    }
    
    @GetMapping("/user/{id}")
    public String userReview(@PathVariable("id")Integer userId,
                             @PageableDefault(page=0,size=10,sort="reservation.date",direction= Sort.Direction.DESC) Pageable pageable,
                             Model model){
        Page<Review> reviews = reviewRepository.findAllByUserId(userId,pageable);
        model.addAttribute("reviews",reviews);
        return "review/user";
    }


}
