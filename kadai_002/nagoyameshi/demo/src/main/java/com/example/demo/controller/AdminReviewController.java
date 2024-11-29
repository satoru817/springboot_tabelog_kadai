package com.example.demo.controller;

import com.example.demo.entity.Review;
import com.example.demo.entity.User;
import com.example.demo.repository.ReviewRepository;
import com.example.demo.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;

@Controller
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/admin/review")
public class AdminReviewController {
    private final ReviewController reviewController;
    private final ReviewRepository reviewRepository;

    @GetMapping("/index")
    public String index(@PageableDefault(page = 0,size = 10, sort="reservation.date", direction = Sort.Direction.DESC)Pageable pageable,
                        @RequestParam(name = "searchQuery",required = false)String searchQuery,
                        Model model){
        Page<Review> reviews;
        if(searchQuery != null && !searchQuery.trim().isEmpty()){
            reviews = reviewRepository.findAllBySearchQuery(searchQuery,pageable);
        }else{
            reviews = reviewRepository.findAll(pageable);
        }

        model.addAttribute("searchQuery",searchQuery);
        model.addAttribute("reviews",reviews);

        return "admin/review/index";
    }

    @Transactional
    @GetMapping("/unmask/{id}")
    public ResponseEntity<?> unmaskReview(@PathVariable("id")Integer reviewId){
        try{
            Review review = reviewRepository.getReferenceById(reviewId);
            review.setIsVisible(true);
            reviewRepository.save(review);

            return ResponseEntity.ok().body("レビューは正常に公開されました");
        }catch(RuntimeException e){
            log.error("review unmasking failure:{}",e.getMessage());
            return ResponseEntity.internalServerError().body("レビューの公開化に失敗しました");
        }
    }

    //レビューを隠すapiのendポイント
    @Transactional
    @GetMapping("/hide/{id}")
    public ResponseEntity<?> hideReview(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                        @PathVariable("id")Integer reviewId,
                                        @RequestParam(name="reason",required = false)String reason){
        User user = userDetails.getUser();
        try{
            Review review = reviewRepository.getReferenceById(reviewId);
            review.setHiddenBy(user);
            review.setIsVisible(false);
            review.setHiddenReason(reason);
            review.setHiddenAt(LocalDateTime.now());

            reviewRepository.save(review);
            return ResponseEntity.ok().body("レビューは正常に非公開化されました");
        }catch (RuntimeException e){
            log.error("review masking failure :{} ",e.getMessage());
            return ResponseEntity.internalServerError().body("レビューの非公開化に失敗しました");
        }
    }
}
