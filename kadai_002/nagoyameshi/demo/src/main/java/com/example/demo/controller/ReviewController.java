package com.example.demo.controller;

import com.example.demo.entity.Reservation;
import com.example.demo.entity.Review;
import com.example.demo.entity.ReviewPhoto;
import com.example.demo.repository.ReservationRepository;
import com.example.demo.repository.ReviewPhotoRepository;
import com.example.demo.repository.ReviewRepository;
import com.example.demo.service.ImageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

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

    //投稿と編集両方に対応する必要がある。
    //遷移先のページではjsを利用した画像の削除ができるようにする。また普通にフォームを
    //送ることができるようにする。
    @GetMapping("/create/{id}")
    public String createReview(@PathVariable("id")Integer reservationId,
                               Model model){
        Reservation reservation = reservationRepository.getReferenceById(reservationId);
        Optional<Review> optionalReview = reviewRepository.findByReservation(reservation);
        Review review = new Review();
        if(optionalReview.isPresent()){
            review = optionalReview.get();
        }else{
            review.setReservation(reservation);
        }

        model.addAttribute("review",review);

        return "review/create";
    }

    //コンテントのfilteringと感情の判断をする
    // 合格した場合のみ登録する。そうでないときは保存しない。
    @Transactional
    @PostMapping("/upsert")
    public String upsertReview(@ModelAttribute Review review){
        String content = review.getContent();
        imageService.saveImageOfReview(review,reviewPhotoRepository);
        reviewRepository.save(review);
        return

    }

    @PostMapping("/deleteImage")
    public String deleteImage(@RequestParam("reviewPhotoId")Integer reviewPhotoId){
        ReviewPhoto reviewPhoto = reviewPhotoRepository.getReferenceById(reviewPhotoId);
        boolean isDeleted = imageService.deleteImage(reviewPhoto.getImageName());
        if(isDeleted){
            log.info("画像は正常に削除されました。");
        }else{
            log.info("画像の削除に失敗しました。");
        }
        Reservation reservation = reviewPhoto.getReview().getReservation();
        reviewPhotoRepository.delete(reviewPhoto);

        return "redirect:/review/create/"+reservation.getCreatedAt();
    }


}
