package com.example.demo.controller;

import com.example.demo.dto.RestaurantCrudForm;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
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
import java.util.ArrayList;
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

    //レストラン一覧画面(管理者用)
    //詳細画面へのリンク、削除ボタン、検索ボックスが必要
    //新規登録ボタンがあってもいい。
    @GetMapping
    public String index(Model model,
                        @PageableDefault(page=0,size=10,sort="restaurantId",direction = Sort.Direction.ASC) Pageable pageable,
                        @RequestParam(name="keyword",required = false)String keyword){
        Page<Restaurant> restaurantsPage;

        if(keyword != null && !keyword.isEmpty()) {
            restaurantsPage = restaurantRepository.findByNameLike("%"+keyword+"%", pageable);
        }else {
            restaurantsPage = restaurantRepository.findAll(pageable);
        }


        model.addAttribute("restaurantsPage",restaurantsPage);

        return "admin/restaurant/index";

    }

    @GetMapping("/{id}")
    public String show(@PathVariable("id")Integer restaurantId,Model model){
        Restaurant restaurant = restaurantRepository.getReferenceById(restaurantId);
        List<CategoryRestaurant> categoryRestaurants = categoryRestaurantRepository.findAllByRestaurant(restaurant);
        List<RestaurantImage> restaurantImages = restaurantImageRepository.findAllByRestaurant(restaurant);

        model.addAttribute("restaurant",restaurant);
        model.addAttribute("categories",getCategories(categoryRestaurants));
        model.addAttribute("restaurantImages",restaurantImages);
        return "admin/restaurant/show";
    }

    private List<Category> getCategories(List<CategoryRestaurant> categoryRestaurants){
        List<Category> categories = new ArrayList<>();
        for(CategoryRestaurant categoryRestaurant:categoryRestaurants){
            categories.add(categoryRestaurant.getCategory());
        }

        return  categories;
    }

    private List<Integer> getCategoryIds(List<CategoryRestaurant> categoryRestaurants){
        List<Integer> categoryIds = new ArrayList<>();
        for(CategoryRestaurant categoryRestaurant:categoryRestaurants){
            categoryIds.add(categoryRestaurant.getCategory().getCategoryId());
        }

        return  categoryIds;
    }

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

    @PostMapping("/category/delete")
    public String deleteCategory(@RequestParam(name="categoryId") Integer id) {
        categoryService.delete(id);
        return "redirect:/admin/restaurant/category/crud"; // 修正
    }

    @GetMapping("/register")
    public String restaurantRegister(Model model){
        if(!model.containsAttribute("restaurantCrudForm")){
            RestaurantCrudForm restaurantCrudForm = new RestaurantCrudForm();
            model.addAttribute("restaurantCrudForm",restaurantCrudForm);
        }
        List<Category> categories = categoryService.fetchAll();
        model.addAttribute("categories",categories);

        return "admin/restaurant/restaurant_register";
    }

    @Transactional
    @PostMapping("/register")
    public String restaurantRegisterExec(@RequestParam("categories") List<Integer> selectedCategoryIds,
                                         @ModelAttribute @Validated RestaurantCrudForm restaurantCrudForm,
                                         BindingResult result,
                                         RedirectAttributes redirectAttributes){

        log.info("selectedCategoryIds:{}",selectedCategoryIds);
        if(result.hasErrors()){
            redirectAttributes.addFlashAttribute("restaurantCrudForm",restaurantCrudForm);
            redirectAttributes.addFlashAttribute(BindingResult.MODEL_KEY_PREFIX
                    + Conventions.getVariableName(restaurantCrudForm),result);
            redirectAttributes.addFlashAttribute("selectedCategoryIds",selectedCategoryIds);
            return "redirect:/admin/restaurant/register"; // 修正
        } else {
            // 同一のメールアドレスまたは電話番号を持つレストランが存在しないか調べる
            Optional<Restaurant> existingRestaurant = restaurantRepository.findByEmailOrPhoneNumber(
                    restaurantCrudForm.getEmail(),
                    restaurantCrudForm.getPhoneNumber()
            );

            if (existingRestaurant.isPresent()) {
                result.rejectValue("email", "error.email", "このメールアドレスまたは電話番号は既に使用されています。");
                redirectAttributes.addFlashAttribute(BindingResult.MODEL_KEY_PREFIX
                        + Conventions.getVariableName(restaurantCrudForm), result);
                return "redirect:/admin/restaurant/register"; // 修正
            }

            // レストランの保存
            Restaurant newRestaurant = restaurantCrudForm.convertToRestaurant();
            restaurantRepository.save(newRestaurant);

            // レストランのカテゴリー情報の保存
            saveCategoryRestaurant(selectedCategoryIds,newRestaurant);

            // レストランの画像の保存
            List<MultipartFile> images = restaurantCrudForm.getImages();
            saveImagesOfRestaurant(images,newRestaurant);

            return "ユーザーの見る詳細画面へのパス";
        }
    }

    @GetMapping("/{id}/edit")
    public String editRestaurant(@PathVariable("id")Integer restaurantId,
                                 Model model){
        Restaurant restaurant = restaurantRepository.getReferenceById(restaurantId);
        RestaurantCrudForm restaurantCrudForm = RestaurantCrudForm.convertToRestaurantCrudForm(restaurant);
        List<Category> allCategories = categoryRepository.findAll();
        List<CategoryRestaurant> categoryRestaurants = categoryRestaurantRepository.findAllByRestaurant(restaurant);
        List<Integer> selectedCategoryIds = getCategoryIds(categoryRestaurants);
        List<RestaurantImage> restaurantImages = restaurantImageRepository.findAllByRestaurant(restaurant);

        model.addAttribute("restaurantCrudForm",restaurantCrudForm);
        model.addAttribute("allCategories",allCategories);
        model.addAttribute("selectedCategoryIds",selectedCategoryIds);
        model.addAttribute("restaurantImages",restaurantImages);

        return "admin/restaurant/restaurant_edit";

    }
//
    @Transactional
    @PostMapping("/{id}/update")
    public String updateRestaurant(@PathVariable("id")Integer restaurantId,
                                   @RequestParam("categories")List<Integer> selectedCategoryIds,
                                   @Validated RestaurantCrudForm restaurantCrudForm,
                                   BindingResult result,
                                   RedirectAttributes redirectAttributes
                                   ){
        if(result.hasErrors()){
            redirectAttributes.addFlashAttribute("restaurantCrudForm",restaurantCrudForm);
            redirectAttributes.addFlashAttribute(BindingResult.MODEL_KEY_PREFIX
            +Conventions.getVariableName(restaurantCrudForm),result);
            redirectAttributes.addFlashAttribute("selectedCategoryIds",selectedCategoryIds);
        }else{
            Restaurant restaurant = restaurantCrudForm.convertToRestaurant();
            restaurantRepository.save(restaurant);//restaurantIdをセットしているのでupsert処理になる。

            saveCategoryRestaurant(selectedCategoryIds,restaurant);

            List<MultipartFile> images = restaurantCrudForm.getImages();
            saveImagesOfRestaurant(images,restaurant);
        }
        return "redirect:/admin/restaurant/"+restaurantId+"/edit";

    }

    //画像削除メソッド
    //レストランと画像の結び付きを切っている。画像自体は削除していない。
    @DeleteMapping("/delete-image")
    @ResponseBody
    public void deleteImage(@RequestParam("restaurantImageId") Integer restaurantImageId) {
        RestaurantImage image = restaurantImageRepository.getReferenceById(restaurantImageId);
        restaurantImageRepository.delete(image);
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

    //複数のレストラン画像を保存する
    public void saveImagesOfRestaurant(List<MultipartFile> images, Restaurant restaurant) {
        log.info("saveImagesOfRestaurantは呼びだされています。");

        // 空でないファイルがあるか確認
        boolean hasNonEmptyFile = images.stream().anyMatch(file -> !file.isEmpty());
        if (hasNonEmptyFile) {
            log.info("imagesには空でないファイルがあります: {}", images);
            for (MultipartFile image : images) {
                if (!image.isEmpty()) {  // 各ファイルが空でないか確認
                    RestaurantImage restaurantImage = new RestaurantImage();
                    String imageName = image.getOriginalFilename();
                    String hashedImageName = generateNewFileName(imageName);
                    Path filePath = Paths.get("src/main/resources/static/images/" + hashedImageName);
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

    //categoryRestaurantを保存する
    public void saveCategoryRestaurant(List<Integer> categoryIds,Restaurant restaurant){
        List<CategoryRestaurant> prevCategoryRestaurant = categoryRestaurantRepository.findAllByRestaurant(restaurant);
        categoryRestaurantRepository.deleteAll(prevCategoryRestaurant);//以前のを全て削除

        for(Integer selectedCategoryId : categoryIds){
            CategoryRestaurant categoryRestaurant = new CategoryRestaurant();
            categoryRestaurant.setRestaurant(restaurant);
            categoryRestaurant.setCategory(categoryRepository.getReferenceById(selectedCategoryId));
            categoryRestaurantRepository.save(categoryRestaurant);
        }
    }


}
