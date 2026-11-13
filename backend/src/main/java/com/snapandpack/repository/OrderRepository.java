package com.snapandpack.repository;

import com.snapandpack.model.CustomerOrder;
import com.snapandpack.model.OrderStatus;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OrderRepository extends JpaRepository<CustomerOrder, Long> {

    @EntityGraph(attributePaths = {"items", "user"})
    List<CustomerOrder> findAllByOrderByCreatedAtDesc();

    @EntityGraph(attributePaths = {"items", "user"})
    List<CustomerOrder> findByUserIdOrderByCreatedAtDesc(Long userId);

    @Query("select coalesce(sum(o.total), 0) from CustomerOrder o where o.status <> :excluded")
    BigDecimal sumTotalExcluding(@Param("excluded") OrderStatus excluded);
}
