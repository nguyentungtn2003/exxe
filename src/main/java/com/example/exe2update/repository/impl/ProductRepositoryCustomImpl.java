package com.example.exe2update.repository.impl;

import com.example.exe2update.entity.Product;
import com.example.exe2update.repository.ProductRepositoryCustom;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.hibernate.query.Query;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class ProductRepositoryCustomImpl implements ProductRepositoryCustom {

    private final EntityManager entityManager;

    public ProductRepositoryCustomImpl(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public Page<Product> findFiltered(int page, int size,
                                      List<Integer> categoryIds,
                                      List<String> discountRanges,
                                      String searchKeyword) {

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();

        // --- Build main query ---
        CriteriaQuery<Product> cq = cb.createQuery(Product.class);
        Root<Product> root = cq.from(Product.class);
        Predicate predicate = cb.conjunction();

        // Apply filters
        predicate = buildFilters(cb, root, predicate, categoryIds, discountRanges, searchKeyword);
        cq.where(predicate);

        // Create and execute data query
        TypedQuery<Product> dataQuery = entityManager.createQuery(cq)
                .setFirstResult(page * size)
                .setMaxResults(size);
        Query<?> hDataQ = dataQuery.unwrap(Query.class);
        System.out.println("JPQL Data Query: " + hDataQ.getQueryString());
        List<Product> content = dataQuery.getResultList();

        // --- Build count query ---
        CriteriaQuery<Long> countCq = cb.createQuery(Long.class);
        Root<Product> countRoot = countCq.from(Product.class);
        Predicate countPredicate = buildFilters(cb, countRoot, cb.conjunction(), categoryIds, discountRanges, searchKeyword);
        countCq.select(cb.count(countRoot)).where(countPredicate);

        TypedQuery<Long> countQuery = entityManager.createQuery(countCq);
        Query<?> hCountQ = countQuery.unwrap(Query.class);
        System.out.println("JPQL Count Query: " + hCountQ.getQueryString());
        Long total = countQuery.getSingleResult();

        return new PageImpl<>(content, PageRequest.of(page, size), total);
    }

    private Predicate buildFilters(CriteriaBuilder cb,
                                   Root<Product> root,
                                   Predicate predicate,
                                   List<Integer> categoryIds,
                                   List<String> discountRanges,
                                   String searchKeyword) {
        // Category filter
        if (categoryIds != null && !categoryIds.isEmpty()) {
            predicate = cb.and(predicate, root.get("category").get("categoryId").in(categoryIds));
        }

        // Discount filter: use > lower and <= upper to match SQL '>5 AND <=20'
        if (discountRanges != null && !discountRanges.isEmpty()) {
            Predicate pDisc = cb.disjunction();
            for (String r : discountRanges) {
                switch (r) {
                    case "0-5":
                        pDisc = cb.or(pDisc,
                                cb.and(
                                        cb.greaterThanOrEqualTo(root.get("discount"), 0.0),
                                        cb.lessThanOrEqualTo(root.get("discount"), 5.0)
                                )
                        );
                        break;
                    case "5-20":
                        pDisc = cb.or(pDisc,
                                cb.and(
                                        cb.greaterThan(root.get("discount"), 5.0),
                                        cb.lessThanOrEqualTo(root.get("discount"), 20.0)
                                )
                        );
                        break;
                    case "20-30":
                        pDisc = cb.or(pDisc,
                                cb.and(
                                        cb.greaterThanOrEqualTo(root.get("discount"), 20.0),
                                        cb.lessThanOrEqualTo(root.get("discount"), 30.0)
                                )
                        );
                        break;
                    case "30-50":
                        pDisc = cb.or(pDisc,
                                cb.and(
                                        cb.greaterThanOrEqualTo(root.get("discount"), 30.0),
                                        cb.lessThanOrEqualTo(root.get("discount"), 50.0)
                                )
                        );
                        break;
                }
            }
            predicate = cb.and(predicate, pDisc);
        }

        // Search keyword filter
        if (searchKeyword != null && !searchKeyword.isBlank()) {
            predicate = cb.and(predicate,
                    cb.like(cb.lower(root.get("name")), "%" + searchKeyword.toLowerCase() + "%")
            );
        }

        return predicate;
    }
}
