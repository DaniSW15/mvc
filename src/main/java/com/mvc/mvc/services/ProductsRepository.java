package com.mvc.mvc.services;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mvc.mvc.models.Product;

public interface ProductsRepository extends JpaRepository<Product, Integer> {

}
