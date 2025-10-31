package com.example.booking.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import com.example.booking.domain.Customer;
import com.example.booking.domain.CustomerFavorite;
import com.example.booking.domain.RestaurantOwner;
import com.example.booking.domain.RestaurantProfile;
import com.example.booking.domain.User;

@DataJpaTest
class CustomerFavoriteRepositoryTest {

    @Autowired
    private CustomerFavoriteRepository repository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    @DisplayName("should query favorites by customer with statistics for owner")
    void shouldQueryFavorites() {
        User ownerUser = new User();
        ownerUser.setUsername("owner");
        ownerUser.setEmail("owner@example.com");
        ownerUser.setPassword("password");
        ownerUser.setFullName("Owner Name");
        entityManager.persist(ownerUser);

        RestaurantOwner owner = new RestaurantOwner();
        owner.setUser(ownerUser);
        owner.setOwnerName("Owner Name");
        entityManager.persist(owner);
        entityManager.flush();
        UUID ownerId = owner.getOwnerId();

        RestaurantProfile r1 = new RestaurantProfile();
        r1.setOwner(owner);
        r1.setRestaurantName("Pho 24");
        r1.setAveragePrice(BigDecimal.valueOf(10));
        entityManager.persist(r1);

        RestaurantProfile r2 = new RestaurantProfile();
        r2.setOwner(owner);
        r2.setRestaurantName("Bun Cha");
        r2.setAveragePrice(BigDecimal.valueOf(12));
        entityManager.persist(r2);

        User customerUser = new User();
        customerUser.setUsername("customer");
        customerUser.setEmail("customer@example.com");
        customerUser.setPassword("password");
        customerUser.setFullName("Customer");
        entityManager.persist(customerUser);

        Customer customer = new Customer();
        customer.setUser(customerUser);
        customer.setFullName("Customer");
        entityManager.persist(customer);
        entityManager.flush();
        UUID customerId = customer.getCustomerId();

        CustomerFavorite f1 = new CustomerFavorite();
        f1.setCustomer(customer);
        f1.setRestaurant(r1);
        f1.setCreatedAt(LocalDateTime.now().minusDays(1));

        CustomerFavorite f2 = new CustomerFavorite();
        f2.setCustomer(customer);
        f2.setRestaurant(r2);
        f2.setCreatedAt(LocalDateTime.now());

        entityManager.persist(f1);
        entityManager.persist(f2);
        entityManager.flush();

        assertThat(repository.existsByCustomerCustomerIdAndRestaurantRestaurantId(customerId, r1.getRestaurantId()))
                .isTrue();

        List<CustomerFavorite> favorites = repository.findByCustomerCustomerIdOrderByCreatedAtDesc(customerId);
        assertThat(favorites).extracting(cf -> cf.getRestaurant().getRestaurantName())
                .containsExactly("Bun Cha", "Pho 24");

        List<Object[]> stats = repository.getFavoriteStatisticsForOwner(ownerId,
                org.springframework.data.domain.PageRequest.of(0, 5));
        assertThat(stats).hasSize(2);

        List<Integer> restaurantIds = repository.findRestaurantIdsByCustomerId(customerId);
        assertThat(restaurantIds).contains(r1.getRestaurantId(), r2.getRestaurantId());

        List<CustomerFavorite> withDetails = repository.findByCustomerWithRestaurantDetails(customerId);
        assertThat(withDetails).allMatch(cf -> cf.getRestaurant().getOwner().getOwnerId().equals(ownerId));
    }
}
