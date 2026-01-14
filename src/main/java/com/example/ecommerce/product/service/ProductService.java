package com.example.ecommerce.product.service;

import com.example.ecommerce.category.entity.Category;
import com.example.ecommerce.category.repository.CategoryRepository;
import com.example.ecommerce.config.exception.BadRequestException;
import com.example.ecommerce.config.exception.CategoryNotFoundExp;
import com.example.ecommerce.config.exception.ProductNotFoundExp;
import com.example.ecommerce.order.entity.OrderItem;
import com.example.ecommerce.order.entity.OrderStatus;
import com.example.ecommerce.order.repo.OrderItemRepository;
import com.example.ecommerce.product.dto.ProductCreateRequest;
import com.example.ecommerce.product.dto.ProductDTO;
import com.example.ecommerce.product.entity.Product;
import com.example.ecommerce.product.mapper.ProductMapper;
import com.example.ecommerce.product.repository.ProductRepository;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final OrderItemRepository orderItemRepository;

    public ProductService(ProductRepository productRepository,
                          CategoryRepository categoryRepository,
                          OrderItemRepository orderItemRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.orderItemRepository = orderItemRepository;
    }
    public static final int PRODUCT_PER_PAGE = 5;

    public List<ProductDTO> findAllProduct() {
        return productRepository.findAll(Sort.by("name").ascending())
                .stream()
                .map(ProductMapper::toDTO)
                .toList();
    }

    public Page<ProductDTO> listByPage(int pageNum, int size, String keyword, Integer categoryId) {
        int pageSize = (size > 0 ? size : PRODUCT_PER_PAGE);
        Pageable pageable = PageRequest.of(
                Math.max(pageNum - 1, 0),
                pageSize,
                Sort.by("name").ascending()
        );

        boolean hasKw = (keyword != null && !keyword.isBlank());
        boolean hasCat = (categoryId != null);

        Page<Product> page;
        if (hasKw && hasCat) {
            page = productRepository.searchByKeywordAndCategory(keyword, categoryId, pageable);
        } else if (hasKw) {
            page = productRepository.findByNameContainingIgnoreCase(keyword.trim(), pageable);
            // page = productRepository.searchP(keyword, pageable);
        } else if (hasCat) {
            page = productRepository.findPageByCategoryId(categoryId, pageable);
        } else {
            page = productRepository.findAll(pageable);
        }

        return page.map(ProductMapper::toDTO);
    }

    public Page<ProductDTO> listByPage(
            int pageNum, int sizePage, String keyword,
            Integer categoryId, String sizeFilter,
            Integer minStock, Integer maxStock) {

        Pageable pageable = PageRequest.of(
                Math.max(pageNum - 1, 0),
                sizePage > 0 ? sizePage : PRODUCT_PER_PAGE,
                Sort.by("name").ascending()
        );

        boolean hasFilter = (sizeFilter != null || minStock != null || maxStock != null || categoryId != null || (keyword != null && !keyword.isBlank()));

        Page<Product> page = hasFilter
                ? productRepository.filterByStockRange(keyword, categoryId, sizeFilter, minStock, maxStock, pageable)
                : productRepository.findAll(pageable);

        return page.map(ProductMapper::toDTO);
    }



    public Map<String, List<ProductDTO>> getProductsByCategory(int num) {
        List<Category> categories = categoryRepository.findAll(Sort.by("name"));
        Map<String, List<ProductDTO>> categoryMap = new HashMap<>();
        Pageable page = PageRequest.of(0, Math.max(num, 1), Sort.by("name"));

        for (Category category : categories) {
            Page<Product> productPage = productRepository.findByCategory_Id(category.getId(), page);
            List<ProductDTO> dtoList = productPage.stream().map(ProductMapper::toDTO).toList();
            categoryMap.put(category.getName(), dtoList);
        }
        return categoryMap;
    }


    public List<Category> findAllCategory() {
        return categoryRepository.findAll();
    }

    public ProductDTO save(ProductCreateRequest request) {
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new CategoryNotFoundExp("Không tìm thấy danh mục với ID: " + request.getCategoryId()));

        String newName = request.getName() == null ? "" : request.getName().trim();
        if (newName.isEmpty()) {
            throw new BadRequestException("Tên sản phẩm không được để trống");
        }

        Product product;
        if (request.getId() == null) {
            if (productRepository.existsByNameIgnoreCase(newName)) {
                throw new BadRequestException("Tên sản phẩm đã tồn tại");
            }
            product = ProductMapper.toEntity(request, category);
            product.setName(newName);
            if (product.getImage() == null || product.getImage().isBlank()) {
                product.setImage(Product.DEFAULT_IMAGE);
            }
            product.setEnabled(true);
        } else {
            product = productRepository.findById(request.getId())
                    .orElseThrow(() -> new ProductNotFoundExp("Không tìm thấy sản phẩm với ID: " + request.getId()));

            if (!product.getName().equalsIgnoreCase(newName)
                    && productRepository.existsByNameIgnoreCase(newName)) {
                throw new BadRequestException("Tên sản phẩm đã tồn tại");
            }

            product.setName(newName);
            product.setPrice(request.getPrice());
            product.setQuantity(request.getQuantity());
            product.setDescription(request.getDescription());
            product.setCategory(category);
            if (request.getImage() != null && !request.getImage().isBlank()) {
                product.setImage(request.getImage());
            }
        }

        try {
            Product saved = productRepository.save(product);
            return ProductMapper.toDTO(saved);
        } catch (org.springframework.dao.DataIntegrityViolationException ex) {
            throw new BadRequestException("Tên sản phẩm đã tồn tại");
        }
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

    @Transactional
    public void delete(Integer id) throws ProductNotFoundExp {
        Product product = getEntityById(id);

        List<OrderItem> relatedItems = product.getOrderItems();

        boolean hasActiveOrders = relatedItems.stream()
                .anyMatch(item -> {
                    OrderStatus status = item.getOrder().getStatus();
                    return status != OrderStatus.COMPLETED && status != OrderStatus.CANCELED;
                });
        if (hasActiveOrders) {
            throw new IllegalStateException("Không thể xoá sản phẩm vì đang nằm trong đơn hàng chưa hoàn tất.");
        }
        productRepository.delete(product);
    }


//    public void delete(Integer id) throws ProductNotFoundExp {
//        Product product = getEntityById(id);
//        productRepository.delete(product);
//    }

    public void updateStatus(Integer id, boolean enabled) {
        productRepository.updateEnabled(id, enabled);
    }

    public boolean isProductNameUnique(Integer id, String name) {
        Product existingProduct = productRepository.getProductByName(name);
        return existingProduct == null || existingProduct.getId().equals(id);
    }

    public Page<ProductDTO> listByCategory(String categoryName, int pageNum) {
        Pageable pageable = PageRequest.of(pageNum - 1, 8);

        Page<Product> productPage = productRepository.findByCategory_Name(categoryName, pageable);
        List<ProductDTO> dtoList = productPage.stream()
                .map(ProductMapper::toDTO)
                .toList();

        return new PageImpl<>(dtoList, pageable, productPage.getTotalElements());
    }

    @Transactional(readOnly = true)
    public List<Product> findForExport(
            String keyword,
            Integer categoryId,
            String sizeFilter,
            Integer minStock,
            Integer maxStock
    ) {
        Pageable pageable = Pageable.unpaged();

        boolean hasFilter =
                sizeFilter != null ||
                        minStock != null ||
                        maxStock != null ||
                        categoryId != null ||
                        (keyword != null && !keyword.isBlank());

        Page<Product> page = hasFilter
                ? productRepository.filterByStockRange(
                keyword, categoryId, sizeFilter, minStock, maxStock, pageable)
                : productRepository.findAll(pageable);

        return page.getContent();
    }
}
