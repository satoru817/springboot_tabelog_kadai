package com.example.demo.controller;

import com.example.demo.dto.RestaurantRegistryForm;
import com.example.demo.entity.Category;
import com.example.demo.entity.CategoryRestaurant;
import com.example.demo.entity.Restaurant;
import com.example.demo.entity.RestaurantImage;
import com.example.demo.repository.CategoryRepository;
import com.example.demo.repository.CategoryRestaurantRepository;
import com.example.demo.repository.RestaurantImageRepository;
import com.example.demo.repository.RestaurantRepository;
import com.example.demo.service.CategoryService;
import com.example.demo.service.RestaurantService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Conventions;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Controller
@RequestMapping("/admin/restaurant")
public class AdminRestaurantController {
    private final CategoryService categoryService;
    private final CategoryRepository categoryRepository;
    private final RestaurantService restaurantService;
    private final RestaurantRepository restaurantRepository;
    private final CategoryRestaurantRepository categoryRestaurantRepository;
    private final RestaurantImageRepository restaurantImageRepository;

    @GetMapping("/category/crud")
    public String registerRestaurantCategory(Model model){
        List<Category> categories = categoryService.fetchAll();
        model.addAttribute("categories",categories);
        return "admin/restaurant/category_edit";
    }

    @PostMapping("/category/create")
    public String createCategory(@ModelAttribute Category category) {
        categoryService.create(category);
        return "redirect:/admin/restaurant/category/crud"; // 修正
    }

    @PostMapping("/category/edit")
    public String editCategory(@ModelAttribute Category category) {
        categoryService.update(category);
        return "redirect:/admin/restaurant/category/crud"; // 修正
    }

    @PostMapping("/category/delete/{id}")
    public String deleteCategory(@PathVariable Integer id) {
        categoryService.delete(id);
        return "redirect:/admin/restaurant/category/crud"; // 修正
    }

    @GetMapping("/register")
    public String restaurantRegister(Model model){
        if(!model.containsAttribute("restaurantRegistryForm")){
            RestaurantRegistryForm restaurantRegistryForm = new RestaurantRegistryForm();
            model.addAttribute("restaurantRegistryForm",restaurantRegistryForm);
        }
        List<Category> categories = categoryService.fetchAll();
        model.addAttribute("categories",categories);

        return "admin/restaurant/restaurant_register";
    }

    @Transactional
    @PostMapping("/register")
    public String restaurantRegisterExec(@RequestParam("categories") List<Integer> selectedCategoryIds,
                                         @ModelAttribute @Validated RestaurantRegistryForm restaurantRegistryForm,
                                         BindingResult result,
                                         RedirectAttributes redirectAttributes){
        if(result.hasErrors()){
            redirectAttributes.addFlashAttribute("restaurantRegistryForm",restaurantRegistryForm);
            redirectAttributes.addFlashAttribute(BindingResult.MODEL_KEY_PREFIX
                    + Conventions.getVariableName(restaurantRegistryForm),result);
            redirectAttributes.addFlashAttribute("selectedCategoryIds",selectedCategoryIds);
            return "redirect:/admin/restaurant/register"; // 修正
        } else {
            // 同一のメールアドレスまたは電話番号を持つレストランが存在しないか調べる
            Optional<Restaurant> existingRestaurant = restaurantRepository.findByEmailOrPhoneNumber(
                    restaurantRegistryForm.getEmail(),
                    restaurantRegistryForm.getPhoneNumber()
            );

            if (existingRestaurant.isPresent()) {
                result.rejectValue("email", "error.email", "このメールアドレスまたは電話番号は既に使用されています。");
                redirectAttributes.addFlashAttribute(BindingResult.MODEL_KEY_PREFIX
                        + Conventions.getVariableName(restaurantRegistryForm), result);
                return "redirect:/admin/restaurant/register"; // 修正
            }

            // レストランの保存
            Restaurant newRestaurant = restaurantRegistryForm.convertToRestaurant();
            restaurantRepository.save(newRestaurant);

            // レストランのカテゴリー情報の保存
            for(Integer selectedCategoryId : selectedCategoryIds){
                CategoryRestaurant categoryRestaurant = new CategoryRestaurant();
                categoryRestaurant.setRestaurant(newRestaurant);
                categoryRestaurant.setCategory(categoryRepository.getReferenceById(selectedCategoryId));
                categoryRestaurantRepository.save(categoryRestaurant);
            }

            // レストランの画像の保存
            List<MultipartFile> images = restaurantRegistryForm.getImages();
            for(MultipartFile image : images){
                RestaurantImage restaurantImage = new RestaurantImage();
                String imageName = image.getOriginalFilename();
                String hashedImageName = generateNewFileName(imageName);
                Path filePath = Paths.get("src/main/resources/static/images/" + hashedImageName);
                try {
                    copyImageFile(image, filePath);
                    restaurantImage.setImageName(hashedImageName);
                    restaurantImage.setRestaurant(newRestaurant);
                    restaurantImageRepository.save(restaurantImage);
                } catch (IOException e) {
                    log.error("画像の保存中にエラーが発生しました: {}", e.getMessage());
                }
            }

            return "ユーザーの見る詳細画面へのパス";
        }
    }

    // UUIDを使って生成したファイル名を返す
    public String generateNewFileName(String fileName) {
        String extension = fileName.substring(fileName.lastIndexOf("."));
        String newFileName = UUID.randomUUID().toString() + extension; // UUID + 拡張子
        return newFileName;
    }

    // 画像ファイルを指定したファイルにコピーする
    public void copyImageFile(MultipartFile imageFile, Path filePath) throws IOException {
        Files.copy(imageFile.getInputStream(), filePath);
    }
}
