package com.example.demo.service;

import com.example.demo.entity.Restaurant;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Service
public class RestaurantService {

    @PersistenceContext
    private EntityManager entityManager;

    public Page<Restaurant> findRestaurantOnCondition(
            String name,
            List<String> wards,
            List<Integer> categoryIds,
            Integer capacity,
            Pageable pageable
    ){
        // JPQLの構築
        StringBuilder jpql = new StringBuilder("SELECT DISTINCT r FROM Restaurant r JOIN r.categoryRestaurants cr WHERE 1=1");//最後にANDを書いてしまわないために1=1がある。
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

        // カテゴリIDフィルター
        if (categoryIds != null && !categoryIds.isEmpty()) {
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
    }
}
