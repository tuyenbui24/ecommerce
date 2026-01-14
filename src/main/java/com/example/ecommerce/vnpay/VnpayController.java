package com.example.ecommerce.vnpay;

import com.example.ecommerce.order.service.OrderService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/api/payments/vnpay")
public class VnpayController {

    @Value("${frontend.base-url}")
    private String feBase;

    private final VnpayService vnp;
    private final OrderService orderService;

    public VnpayController(VnpayService vnp, OrderService orderService) {
        this.vnp = vnp; this.orderService = orderService;
    }

    public static class CreatePaymentReq {
        public Integer orderId;
    }

    @PostMapping("/create")
    public ResponseEntity<?> create(@RequestBody CreatePaymentReq req, HttpServletRequest r) {
        long amountVnd = orderService.getAmountVnd(req.orderId);
        orderService.assertOrderReady(req.orderId, amountVnd);

        String ip = Optional.ofNullable(r.getHeader("X-Forwarded-For"))
                .orElse(r.getRemoteAddr());
        if (ip.contains(",")) ip = ip.split(",")[0].trim();
        if ("0:0:0:0:0:0:0:1".equals(ip) || "::1".equals(ip)) ip = "127.0.0.1";

        String url = vnp.createPaymentUrl(String.valueOf(req.orderId), amountVnd, ip);
        return ResponseEntity.ok(Map.of("payUrl", url));
    }

    @GetMapping("/return")
    public ResponseEntity<?> vnpayReturn(HttpServletRequest request) {
        Map<String, String[]> raw = request.getParameterMap();
        Map<String, String> params = new HashMap<>();
        raw.forEach((k,v) -> params.put(k, v != null && v.length > 0 ? v[0] : ""));

        Map<String,String> verifyMap = new HashMap<>(params);
        boolean ok = vnp.verifySignature(verifyMap);

        String vnpTxnRef = params.getOrDefault("vnp_TxnRef", "0");
        String[] parts = vnpTxnRef.split("-");
        Integer orderId = Integer.valueOf(parts[0]);

        long amountVnd = 0L;
        try {
            amountVnd = Long.parseLong(params.getOrDefault("vnp_Amount","0")) / 100;
        } catch (Exception ignore) {}

        if (!ok) {
            orderService.markFailedByVnpay(orderId, amountVnd, params);
            return ResponseEntity.status(303)
                    .header("Location", feBase + "/payment/vnpay-result?status=fail&reason=sign")
                    .build();
        }

        String rsp = params.getOrDefault("vnp_ResponseCode", "");
        try {
            if ("00".equals(rsp)) {
                orderService.markPaidByVnpay(orderId, amountVnd, params);
                return ResponseEntity.status(303)
                        .header("Location", feBase + "/payment/vnpay-result?status=success&orderId=" + orderId)
                        .build();
            } else {
                orderService.markFailedByVnpay(orderId, amountVnd, params);
                return ResponseEntity.status(303)
                        .header("Location", feBase + "/payment/vnpay-result?status=fail&code=" + rsp)
                        .build();
            }
        } catch (Exception ex) {
            orderService.markFailedByVnpay(orderId, amountVnd, params);
            return ResponseEntity.status(303)
                    .header("Location", feBase + "/payment/vnpay-result?status=fail&reason=server")
                    .build();
        }
    }


//    @RequestMapping(value = "/ipn", method = {RequestMethod.GET, RequestMethod.POST})
//    public ResponseEntity<?> ipn(@RequestParam Map<String,String> params) {
//        log.info("[VNPAY][IPN] params={}", params);
//        Map<String,String> copy = new HashMap<>(params);
//        String recv = copy.get("vnp_SecureHash");
//        boolean ok = vnp.verifySignature(copy);
//        log.info("[VNPAY][IPN] verify={} recvHash={} ", ok, recv);
//        if (!ok) return ResponseEntity.ok(Map.of("RspCode","97","Message","Invalid signature"));
//        Integer orderId = Integer.valueOf(params.get("vnp_TxnRef"));
//        long amount = Long.parseLong(params.get("vnp_Amount")) / 100;
//        String rsp = params.get("vnp_ResponseCode");
//
//        try {
//            if ("00".equals(rsp)) orderService.markPaidByVnpay(orderId, amount, params);
//            else                  orderService.markFailedByVnpay(orderId, amount, params);
//            return ResponseEntity.ok(Map.of("RspCode","00","Message","Confirm Success"));
//        } catch (Exception e) {
//            return ResponseEntity.ok(Map.of("RspCode","99","Message","Process error"));
//        }
//    }

    @PostMapping("/pay-again/{orderId}")
    public ResponseEntity<?> payAgain(@PathVariable Integer orderId, HttpServletRequest r) {
        long amountVnd = orderService.getAmountVnd(orderId);

        if (!orderService.isRetryable(orderId)) {
            throw new IllegalStateException("Chỉ có thể thanh toán lại đơn hàng thất bại hoặc đang chờ.");
        }

        orderService.updateStatus(orderId, "PENDING");

        String ip = Optional.ofNullable(r.getHeader("X-Forwarded-For"))
                .orElse(r.getRemoteAddr());
        if (ip.contains(",")) ip = ip.split(",")[0].trim();
        if ("0:0:0:0:0:0:0:1".equals(ip) || "::1".equals(ip)) ip = "127.0.0.1";

        String url = vnp.createPaymentUrl(String.valueOf(orderId), amountVnd, ip);
        return ResponseEntity.ok(Map.of("payUrl", url));
    }
}
