package com.example.demo.service;

import com.example.demo.dto.OpeningHours;
import com.example.demo.entity.Reservation;
import com.example.demo.entity.Restaurant;
import com.example.demo.entity.User;
import com.example.demo.repository.RestaurantRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@RequiredArgsConstructor
@Service
public class RestaurantService {
    private final RestaurantRepository restaurantRepository;

    private String formatLocalTime(LocalTime time) {
        if (time == null) return null;
        return time.format(DateTimeFormatter.ofPattern("HH:mm"));
    }

    public OpeningHours getOpeningHours(Integer restaurantId, LocalDate date){
        Restaurant restaurant = restaurantRepository.getReferenceById(restaurantId);

        OpeningHours openingHours = new OpeningHours();

        DayOfWeek dayOfWeek = date.getDayOfWeek();
        String day = dayOfWeek.toString().toLowerCase();

        String hours = restaurant.getOpeningHoursForDay(day);

        if (hours.equals("休業")) {
            openingHours.setIsBusinessDay(false);
            openingHours.setOpeningTime(null);
            openingHours.setClosingTime(null);
        } else if (hours.equals("未設定")) {
            openingHours.setIsBusinessDay(false);
            openingHours.setOpeningTime(null);
            openingHours.setClosingTime(null);
        } else {
            openingHours.setIsBusinessDay(true);

            // 営業時間文字列からLocalTimeを取得
            switch (day) {
                case "monday":
                    openingHours.setOpeningTime(formatLocalTime(restaurant.getMondayOpeningTime()));
                    openingHours.setClosingTime(formatLocalTime(restaurant.getMondayClosingTime()));
                    break;
                case "tuesday":
                    openingHours.setOpeningTime(formatLocalTime(restaurant.getTuesdayOpeningTime()));
                    openingHours.setClosingTime(formatLocalTime(restaurant.getTuesdayClosingTime()));
                    break;
                case "wednesday":
                    openingHours.setOpeningTime(formatLocalTime(restaurant.getWednesdayOpeningTime()));
                    openingHours.setClosingTime(formatLocalTime(restaurant.getWednesdayClosingTime()));
                    break;
                case "thursday":
                    openingHours.setOpeningTime(formatLocalTime(restaurant.getThursdayOpeningTime()));
                    openingHours.setClosingTime(formatLocalTime(restaurant.getThursdayClosingTime()));
                    break;
                case "friday":
                    openingHours.setOpeningTime(formatLocalTime(restaurant.getFridayOpeningTime()));
                    openingHours.setClosingTime(formatLocalTime(restaurant.getFridayClosingTime()));
                    break;
                case "saturday":
                    openingHours.setOpeningTime(formatLocalTime(restaurant.getSaturdayOpeningTime()));
                    openingHours.setClosingTime(formatLocalTime(restaurant.getSaturdayClosingTime()));
                    break;
                case "sunday":
                    openingHours.setOpeningTime(formatLocalTime(restaurant.getSundayOpeningTime()));
                    openingHours.setClosingTime(formatLocalTime(restaurant.getSundayClosingTime()));
                    break;
            }

        }

        return openingHours;
    }

    @PersistenceContext
    private EntityManager entityManager;

    public Page<Restaurant> findRestaurantOnCondition(
            String name,
            List<String> wards,
            List<Integer> categoryIds,
            Integer capacity,
            String logic,
            Pageable pageable
    ){

        if(logic!=null && logic.equals("or")){
// JPQLの構築
            StringBuilder jpql = new StringBuilder("SELECT DISTINCT r FROM Restaurant r JOIN r.categoryRestaurants cr WHERE 1=1");//全部ANDから始められるように
            List<String> conditions = new ArrayList<>();

            // 動的パラメータ
            List<Object> parameters = new ArrayList<>();
            int paramIndex = 1;

            // 名前フィルター
            if (StringUtils.hasText(name)) {
                jpql.append(" AND r.name LIKE ?").append(paramIndex);
                parameters.add("%" + name + "%");
                paramIndex++;
            }

            // 地域フィルター
            if (wards != null && !wards.isEmpty()) {
                jpql.append(" AND (");
                for (int i = 0; i < wards.size(); i++) {
                    jpql.append("r.address LIKE ?").append(paramIndex);
                    parameters.add("%" + wards.get(i) + "%");
                    if (i < wards.size() - 1) {
                        jpql.append(" OR ");
                    }
                    paramIndex++;
                }
                jpql.append(")");
            }

            // カテゴリIDフィルター(OR検索時)
            if (categoryIds != null && !categoryIds.isEmpty() ) {
                jpql.append(" AND cr.category.categoryId IN ?").append(paramIndex);
                parameters.add(categoryIds);
                paramIndex++;
            }


            // 収容人数フィルター
            if (capacity != null) {
                jpql.append(" AND r.capacity >= ?").append(paramIndex);
                parameters.add(capacity);
                paramIndex++;
            }

            // クエリ作成
            TypedQuery<Restaurant> query = entityManager.createQuery(jpql.toString(), Restaurant.class);

            // パラメータの設定
            for (int i = 0; i < parameters.size(); i++) {
                query.setParameter(i + 1, parameters.get(i));
            }

            // ページネーション設定
            query.setFirstResult((int) pageable.getOffset());//ページ番号が2(0始まり）で一ページに10件のレコードを表示するなら、2*10の20がオフセット。21番目からデータベースからとってくる。
            query.setMaxResults(pageable.getPageSize());

            // 結果の取得
            List<Restaurant> restaurants = query.getResultList();

            // 件数取得用のクエリ
            String countJpql = jpql.toString().replace("SELECT DISTINCT r", "SELECT COUNT(DISTINCT r)");
            TypedQuery<Long> countQuery = entityManager.createQuery(countJpql, Long.class);

            for (int i = 0; i < parameters.size(); i++) {
                countQuery.setParameter(i + 1, parameters.get(i));
            }

            Long total = countQuery.getSingleResult();

            return new PageImpl<>(restaurants, pageable, total);//PageImple<Restaurant>が作られる。ダイヤモンド演算子を使うと型を推測してくれる。
        }else {
            // JPQLの構築
            StringBuilder jpql = new StringBuilder("SELECT DISTINCT r FROM Restaurant r JOIN r.categoryRestaurants cr WHERE 1=1");

            // 動的パラメータ
            List<Object> parameters = new ArrayList<>();
            int paramIndex = 1;

            // 名前フィルター
            if (StringUtils.hasText(name)) {
                jpql.append(" AND r.name LIKE ?").append(paramIndex);
                parameters.add("%" + name + "%");
                paramIndex++;
            }

            // 地域フィルター
            if (wards != null && !wards.isEmpty()) {
                jpql.append(" AND (");
                for (int i = 0; i < wards.size(); i++) {
                    jpql.append("r.address LIKE ?").append(paramIndex);
                    parameters.add("%" + wards.get(i) + "%");
                    if (i < wards.size() - 1) {
                        jpql.append(" OR ");
                    }
                    paramIndex++;
                }
                jpql.append(")");
            }

            // カテゴリIDフィルター (AND検索時)
            if (categoryIds != null && !categoryIds.isEmpty()) {
                // カテゴリの数と一致するか確認するためのサブクエリを追加
                jpql.append(" AND (SELECT COUNT(cr.category.categoryId) FROM CategoryRestaurant cr ")
                        .append("WHERE cr.restaurant = r AND cr.category.categoryId IN ?").append(paramIndex)
                        .append(") = ").append(categoryIds.size());
                parameters.add(categoryIds);
                paramIndex++;
            }

            // 収容人数フィルター
            if (capacity != null) {
                jpql.append(" AND r.capacity >= ?").append(paramIndex);
                parameters.add(capacity);
                paramIndex++;
            }

            // クエリ作成
            TypedQuery<Restaurant> query = entityManager.createQuery(jpql.toString(), Restaurant.class);

            // パラメータの設定
            for (int i = 0; i < parameters.size(); i++) {
                query.setParameter(i + 1, parameters.get(i));
            }

            // ページネーション設定
            query.setFirstResult((int) pageable.getOffset());
            query.setMaxResults(pageable.getPageSize());

            // 結果の取得
            List<Restaurant> restaurants = query.getResultList();

            // 件数取得用のクエリ
            String countJpql = jpql.toString().replace("SELECT DISTINCT r", "SELECT COUNT( DISTINCT r)");
            TypedQuery<Long> countQuery = entityManager.createQuery(countJpql, Long.class);

            for (int i = 0; i < parameters.size(); i++) {
                countQuery.setParameter(i + 1, parameters.get(i));
            }

            Long total = countQuery.getSingleResult();

            return new PageImpl<>(restaurants, pageable, total);

        }
    }


}
