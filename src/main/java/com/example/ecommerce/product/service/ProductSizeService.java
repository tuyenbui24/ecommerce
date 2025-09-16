package com.example.ecommerce.product.service;

import com.example.ecommerce.product.dto.ProductSizeCreateRequest;
import com.example.ecommerce.product.dto.ProductSizeDTO;
import com.example.ecommerce.product.entity.Product;
import com.example.ecommerce.product.entity.ProductSize;
import com.example.ecommerce.product.mapper.ProductMapper;
import com.example.ecommerce.product.repository.ProductRepository;
import com.example.ecommerce.product.repository.ProductSizeRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
public class ProductSizeService {

    private final ProductSizeRepository sizeRepo;
    private final ProductRepository productRepo;

    public ProductSizeService(ProductSizeRepository sizeRepo, ProductRepository productRepo) {
        this.sizeRepo = sizeRepo;
        this.productRepo = productRepo;
    }

    public List<ProductSizeDTO> getSizesByProduct(Integer productId) {
        return sizeRepo.findByProduct_Id(productId)
                .stream()
                .map(ProductMapper::toSizeDTO)
                .toList();
    }

    public ProductSizeDTO addSize(ProductSizeCreateRequest request) {
        Product product = productRepo.findById(request.getProductId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm"));

        ProductSize size = ProductSize.builder()
                .size(request.getSize())
                .quantity(request.getQuantity())
                .product(product)
                .build();

        ProductSize saved = sizeRepo.save(size);
        return ProductMapper.toSizeDTO(saved);
    }

    public ProductSizeDTO updateSize(Integer id, ProductSizeCreateRequest request) {
        ProductSize size = sizeRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy size với ID: " + id));

        size.setSize(request.getSize());
        size.setQuantity(request.getQuantity());

        ProductSize updated = sizeRepo.save(size);
        return ProductMapper.toSizeDTO(updated);
    }

    @Transactional
    public void deleteSize(Integer id) {
        int rows = sizeRepo.hardDeleteById(id);
        if (rows == 0) throw new RuntimeException("Không tìm thấy size với ID: " + id);
        log.info("Deleted product_size id={} (rows={})", id, rows);
        sizeRepo.flush();
    }


//    public void addOrUpdateSize(Integer productId, String size, Integer quantity) {
//        var sizes = sizeRepo.findByProduct_Id(productId);
//        for (var s : sizes) {
//            if (s.getSize().equals(size)) {
//                s.setQuantity(quantity);
//                sizeRepo.save(s);
//                return;
//            }
//        }
//        // Nếu chưa có thì thêm mới
//        Product product = productRepo.findById(productId).orElseThrow();
//        ProductSize newSize = ProductSize.builder()
//                .size(size)
//                .quantity(quantity)
//                .product(product)
//                .build();
//        sizeRepo.save(newSize);
//    }
}
