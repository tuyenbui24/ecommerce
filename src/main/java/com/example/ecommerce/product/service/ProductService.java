package com.example.ecommerce.product.service;

import com.example.ecommerce.category.entity.Category;
import com.example.ecommerce.category.repository.CategoryRepository;
import com.example.ecommerce.config.exception.CategoryNotFoundExp;
import com.example.ecommerce.config.exception.ProductNotFoundExp;
import com.example.ecommerce.product.dto.ProductCreateRequest;
import com.example.ecommerce.product.dto.ProductDTO;
import com.example.ecommerce.product.entity.Product;
import com.example.ecommerce.product.mapper.ProductMapper;
import com.example.ecommerce.product.repository.ProductRepository;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    public static final int PRODUCT_PER_PAGE = 5;

    public ProductService(ProductRepository productRepository, CategoryRepository categoryRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
    }

    public List<ProductDTO> findAllProduct() {
        return productRepository.findAll(Sort.by("name").ascending())
                .stream()
                .map(ProductMapper::toDTO)
                .toList();
    }

    public Page<ProductDTO> listByPage(int pageNum, String keyword) {
        Pageable pageable = PageRequest.of(pageNum - 1, PRODUCT_PER_PAGE, Sort.by("name").ascending());
        Page<Product> page;

        if (keyword != null && !keyword.isBlank()) {
            page = productRepository.searchP(keyword, pageable);
        } else {
            page = productRepository.findAll(pageable);
        }

        List<ProductDTO> dtos = page.map(ProductMapper::toDTO).toList();
        return new PageImpl<>(dtos, pageable, page.getTotalElements());
    }

    public List<Category> findAllCategory() {
        return categoryRepository.findAll();
    }

    public ProductDTO save(ProductCreateRequest request) {
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new CategoryNotFoundExp("Không tìm thấy danh mục với ID: " + request.getCategoryId()));

        Product product;

        if (request.getId() == null) {
            // Tạo mới
            product = ProductMapper.toEntity(request, category);
        } else {
            // Cập nhật
            product = productRepository.findById(request.getId())
                    .orElseThrow(() -> new ProductNotFoundExp("Không tìm thấy sản phẩm với ID: " + request.getId()));

            product.setName(request.getName());
            product.setPrice(request.getPrice());
            product.setQuantity(request.getQuantity());
            product.setDescription(request.getDescription());
            product.setCategory(category);

            if (request.getImage() != null && !request.getImage().isEmpty()) {
                product.setImage(request.getImage());
            }
        }

        Product saved = productRepository.save(product);
        return ProductMapper.toDTO(saved);
    }

    public ProductDTO getDtoById(Integer id) throws ProductNotFoundExp {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundExp("Không tìm thấy sản phẩm với ID: " + id));
        return ProductMapper.toDTO(product);
    }

    public Product getEntityById(Integer id) throws ProductNotFoundExp {
        return productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundExp("Không tìm thấy sản phẩm với ID: " + id));
    }

    public void delete(Integer id) throws ProductNotFoundExp {
        Product product = getEntityById(id);
        productRepository.delete(product);
    }

    public void updateStatus(Integer id, boolean enabled) {
        productRepository.updateEnabled(id, enabled);
    }

    public boolean isProductNameUnique(Integer id, String name) {
        Product existingProduct = productRepository.getProductByName(name);
        return existingProduct == null || existingProduct.getId().equals(id);
    }

    public Map<String, List<ProductDTO>> getProductsByCategory(int num) {
        List<Category> categories = categoryRepository.findAll(Sort.by("name"));
        Map<String, List<ProductDTO>> categoryMap = new HashMap<>();

        for (Category category : categories) {
            Pageable page = PageRequest.of(0, num, Sort.by("name"));
            List<Product> productList = productRepository.findByCategory_Id(category.getId(), page);
            List<ProductDTO> dtoList = productList.stream().map(ProductMapper::toDTO).toList();
            categoryMap.put(category.getName(), dtoList);
        }

        return categoryMap;
    }

    public Page<ProductDTO> listByCategory(String categoryName, int pageNum) {
        Pageable pageable = PageRequest.of(pageNum - 1, 8);

        Page<Product> productPage = productRepository.findByCategory_Name(categoryName, pageable);
        List<ProductDTO> dtoList = productPage.stream()
                .map(ProductMapper::toDTO)
                .toList();

        return new PageImpl<>(dtoList, pageable, productPage.getTotalElements());
    }
}
