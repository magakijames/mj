package com.snapandpack.repository;

import com.snapandpack.model.Product;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/** Database access for products. */
public interface ProductRepository extends JpaRepository<Product, Long> {

    /** Every product in the order it was added. */
    List<Product> findAllByOrderByIdAsc();

    /**
     * Takes stock only if enough is left, in one statement, so two shoppers
     * cannot both buy the last item. Returns 1 when it worked and 0 when stock ran short.
     */
    @Modifying
    @Query("update Product p set p.stock = p.stock - :quantity where p.id = :id and p.stock >= :quantity")
    int takeStock(@Param("id") Long id, @Param("quantity") int quantity);

    /** Puts stock back on sale, used when an order is cancelled. */
    @Modifying
    @Query("update Product p set p.stock = p.stock + :quantity where p.id = :id")
    int returnStock(@Param("id") Long id, @Param("quantity") int quantity);
}
