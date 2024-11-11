package com.example.demo.service;

import com.example.demo.entity.Restaurant;
import com.example.demo.entity.RestaurantImage;
import com.example.demo.entity.Review;
import com.example.demo.entity.ReviewPhoto;
import com.example.demo.repository.RestaurantImageRepository;
import com.example.demo.repository.RestaurantRepository;
import com.example.demo.repository.ReviewPhotoRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class ImageService {
    private static final String IMAGE_DIRECTORY = "src/main/resources/static/images/";

    // UUIDを使って生成したファイル名を返す
    public String generateNewFileName(String fileName) {
        String extension = fileName.substring(fileName.lastIndexOf("."));
        // UUID + 拡張子
        return UUID.randomUUID().toString() + extension;
    }

    // 画像ファイルを指定したファイルにコピーする
    public  void copyImageFile(MultipartFile imageFile, Path filePath) throws IOException {
        Files.copy(imageFile.getInputStream(), filePath);
    }

    //複数のレストラン画像を保存する
    public void saveImagesOfRestaurant(RestaurantImageRepository restaurantImageRepository, List<MultipartFile> images, Restaurant restaurant) {
        log.info("saveImagesOfRestaurantは呼びだされています。");

        // 空でないファイルがあるか確認
        boolean hasNonEmptyFile = images.stream().anyMatch(file -> !file.isEmpty());
        if (hasNonEmptyFile) {
            log.info("imagesには空でないファイルがあります: {}", images);
            for (MultipartFile image : images) {
                if (!image.isEmpty()) {  // 各ファイルが空でないか確認
                    RestaurantImage restaurantImage = new RestaurantImage();
                    String imageName = image.getOriginalFilename();
                    assert imageName != null;
                    String hashedImageName = generateNewFileName(imageName);
                    Path filePath = Paths.get(IMAGE_DIRECTORY + hashedImageName);
                    try {
                        copyImageFile(image, filePath);
                        restaurantImage.setImageName(hashedImageName);
                        restaurantImage.setRestaurant(restaurant);
                        restaurantImageRepository.save(restaurantImage);
                    } catch (IOException e) {
                        log.error("画像の保存中にエラーが発生しました: {}", e.getMessage());
                    }
                } else {
                    log.info("空のファイルが含まれていました。");
                }
            }
        } else {
            log.info("imagesはすべてemptyでした。");
        }
    }

    //複数のReviewPhotoを作成して保存する。
    public void saveImageOfReview(Review review, ReviewPhotoRepository reviewPhotoRepository){
        List<MultipartFile> images = review.getImages();
        if(images!=null&&!images.isEmpty()){
           // 空でないファイルがあるか確認
            boolean hasNonEmptyFile = images.stream().anyMatch(file -> !file.isEmpty());
            if(hasNonEmptyFile){
                for(MultipartFile image: images){
                    if(!image.isEmpty()){
                        ReviewPhoto reviewPhoto = new ReviewPhoto();
                        String imageName = image.getOriginalFilename();
                        assert imageName != null;
                        String hashedImageName = generateNewFileName(imageName);
                        Path filePath = Paths.get(IMAGE_DIRECTORY + hashedImageName);
                        try{
                            copyImageFile(image,filePath);
                            reviewPhoto.setReview(review);
                            reviewPhoto.setImageName(hashedImageName);
                            reviewPhotoRepository.save(reviewPhoto);
                        }catch(IOException e){
                            log.error("画像の保存中にエラーが発生しました。:{}",e.getMessage());
                        }
                    }else{
                        log.info("空の画像ファイルが含まれていました。");
                    }
                }
            }else{
                log.info("imagesは全て空でした。");
            }
        }

    }



    public boolean deleteImage(String fileName) {
        Path imagePath = Paths.get(IMAGE_DIRECTORY, fileName);
        try {
            return Files.deleteIfExists(imagePath);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
}
