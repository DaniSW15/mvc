package com.mvc.mvc.controller;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.mvc.mvc.models.Product;
import com.mvc.mvc.models.ProductDto;
import com.mvc.mvc.services.ProductsRepository;

import jakarta.validation.Valid;





@Controller
@RequestMapping("/products")
public class ProductsController {

    @Autowired
    private ProductsRepository productsRepository;

    @GetMapping({"", "/"})
    public String showProductList(Model model) {
        List<Product> products = productsRepository.findAll();
        model.addAttribute("products", products);
        return "products/index";
    }

    @GetMapping("/create")
    public String showCreatePage(Model model) {
        ProductDto productDto = new ProductDto();
        model.addAttribute("productDto", productDto);
        return "products/CreateProduct";
    }

    @PostMapping("/create")
    public String createProduct(@Valid @ModelAttribute ProductDto productDto, BindingResult result) {

        if (productDto.getImagenFile().isEmpty()) {
            result.addError(new FieldError("productDto", "imagenFile", "Image file is required"));
        }

        if (result.hasErrors()) {
            return "products/CreateProduct";
        }

        MultipartFile image = productDto.getImagenFile();
        Date createdAt = new Date();
        String storageFileName = createdAt.getTime() + "_" + image.getOriginalFilename();

        try {
            String uploadDir = "public/images/";
            Path uploadPath = Paths.get(uploadDir);

            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            try (
                InputStream inputStream = image.getInputStream();
            ) {
                Files.copy(inputStream, Paths.get(uploadDir + storageFileName), StandardCopyOption.REPLACE_EXISTING);
            }

        } catch (Exception ex) {
            System.out.println("Error uploading image: " + ex.getMessage());
        }

        Product product = new Product();
        product.setName(productDto.getName());
        product.setBrand(productDto.getBrand());
        product.setCategory(productDto.getCategory());
        product.setPrice(productDto.getPrice());
        product.setDescription(productDto.getDescription());
        product.setCreatedAt(createdAt);
        product.setImageFileName(storageFileName);

        productsRepository.save(product);

        return "redirect:/products";
    }
    
    @GetMapping("/edit")
    public String showEditPage(Model model, @RequestParam int id) {
        try {
            Product product = productsRepository.findById(id).get();
            model.addAttribute("product", product);

            ProductDto productDto = new ProductDto();
            productDto.setName(product.getName());
            productDto.setBrand(product.getBrand());
            productDto.setCategory(product.getCategory());
            productDto.setPrice(product.getPrice());
            productDto.setDescription(product.getDescription());
            // productDto.setImagenFile(null);

            model.addAttribute("productDto", productDto);
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            return "redirect:/products";
        }
        return "products/EditProduct";
    }

    @PostMapping("/edit")
    public String editProduct(Model model, @Valid @ModelAttribute ProductDto productDto, BindingResult result, @RequestParam int id) {
        if (result.hasErrors()) {
            return "products/EditProduct";
        }

        try {
            Product product = productsRepository.findById(id).get();
            model.addAttribute("product", product);
            product.setName(productDto.getName());
            product.setBrand(productDto.getBrand());
            product.setCategory(productDto.getCategory());
            product.setPrice(productDto.getPrice());
            product.setDescription(productDto.getDescription());

            MultipartFile image = productDto.getImagenFile();
            if (!image.isEmpty()) {
                Date createdAt = new Date();
                String storageFileName = createdAt.getTime() + "_" + image.getOriginalFilename();

                try {
                    String uploadDir = "public/images/";
                    Path uploadPath = Paths.get(uploadDir + product.getImageFileName());

                    try {
                        Files.delete(uploadPath);
                    } catch (Exception e) {
                        System.out.println("Error deleting image: " + e.getMessage());
                    }

                    if (!Files.exists(uploadPath)) {
                        Files.createDirectories(uploadPath);
                    }

                    try (
                        InputStream inputStream = image.getInputStream();
                    ) {
                        Files.copy(inputStream, Paths.get(uploadDir + storageFileName), StandardCopyOption.REPLACE_EXISTING);
                    }
                    
                    product.setImageFileName(storageFileName);
                } catch (Exception ex) {
                    System.out.println("Error uploading image: " + ex.getMessage());
                }
            }

            productsRepository.save(product);
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }

        return "redirect:/products";
    }

    @GetMapping("/delete")
    public String deleteProduct(@RequestParam int id) {
        try {
            Product product = productsRepository.findById(id).get();
            productsRepository.delete(product);
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
        return "redirect:/products";
    }
    
}
