package com.example.booking.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.booking.domain.Customer;
import com.example.booking.repository.CustomerRepository;

@Service
@Transactional
public class CustomerService {
    
    @Autowired
    private CustomerRepository customerRepository;
    
    /**
     * Tìm customer theo username
     */
    @Transactional(readOnly = true)
    public Optional<Customer> findByUsername(String username) {
        return customerRepository.findByUserUsername(username);
    }
    
    /**
     * Tìm customer theo ID
     */
    @Transactional(readOnly = true)
    public Optional<Customer> findById(UUID customerId) {
        return customerRepository.findById(customerId);
    }
    
    /**
     * Lưu customer
     */
    public Customer save(Customer customer) {
        return customerRepository.save(customer);
    }
    
    /**
     * Tìm customer theo user ID
     */
    @Transactional(readOnly = true)
    public Optional<Customer> findByUserId(UUID userId) {
        return customerRepository.findByUserId(userId);
    }
    
    /**
     * Lấy tất cả customers
     */
    @Transactional(readOnly = true)
    public List<Customer> findAllCustomers() {
        return customerRepository.findAll();
    }
}
