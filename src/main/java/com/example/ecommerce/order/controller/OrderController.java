package com.example.ecommerce.order.controller;

import com.example.ecommerce.order.dto.OrderDTO;
import com.example.ecommerce.order.service.OrderService;
import com.example.ecommerce.security.SecurityUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/orders")
public class OrderController {

    private final OrderService orderService;
    private final SecurityUtils securityUtils;

    public OrderController(OrderService orderService, SecurityUtils securityUtils) {
        this.orderService = orderService;
        this.securityUtils = securityUtils;
    }

    // GET: Hiển thị form nhập địa chỉ nhận hàng
    @GetMapping("/checkout")
    public String showCheckoutPage() {
        return "order_checkout";
    }

    // POST: Xử lý đặt hàng
    @PostMapping("/checkout")
    public String placeOrder(@RequestParam("address") String shippingAddress,
                             @RequestParam(value = "note", required = false) String note,
                             RedirectAttributes ra) {
        Integer userId = securityUtils.getCurrentUserId();
        orderService.createOrder(userId, shippingAddress, note);
        ra.addFlashAttribute("message", "Đặt hàng thành công!");
        return "redirect:/orders/history";
    }

    // GET: Xem lịch sử đơn hàng
    @GetMapping("/history")
    public String orderHistory(Model model) {
        Integer userId = securityUtils.getCurrentUserId();
        List<OrderDTO> orders = orderService.getOrderHistory(userId);
        model.addAttribute("orders", orders);
        return "order_history";
    }

    @GetMapping("/{id}")
    public String viewOrderDetails(@PathVariable Integer id, Model model) {
        OrderDTO order = orderService.getById(id);
        model.addAttribute("order", order);
        return "order_detail";
    }

    @GetMapping("/admin")
    public String listAllOrders(Model model) {
        List<OrderDTO> orders = orderService.getAllOrders();
        model.addAttribute("orders", orders);
        return "admin_order_list";
    }

    @PostMapping("/admin/update-status")
    public String updateOrderStatus(@RequestParam("orderId") Integer orderId,
                                    @RequestParam("status") String status,
                                    RedirectAttributes ra) {
        orderService.updateStatus(orderId, status);
        ra.addFlashAttribute("message", "Cập nhật trạng thái thành công!");
        return "redirect:/orders/admin";
    }
}

