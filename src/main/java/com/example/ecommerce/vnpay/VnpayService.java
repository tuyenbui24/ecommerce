package com.example.ecommerce.vnpay;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class VnpayService {
    @Value("${vnpay.tmn-code}") private String tmnCode;
    @Value("${vnpay.hash-secret}") private String hashSecret;
    @Value("${vnpay.pay-url}") private String payUrl;
    @Value("${vnpay.return-url}") private String returnUrl;

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    public String createPaymentUrl(String orderId, long amountVnd, String clientIp) {
        String txnRef = orderId + "-" + System.currentTimeMillis();

        Map<String,String> p = new HashMap<>();
        p.put("vnp_Version","2.1.0");
        p.put("vnp_Command","pay");
        p.put("vnp_TmnCode", tmnCode);
        p.put("vnp_Amount", String.valueOf(amountVnd * 100));
        p.put("vnp_CurrCode","VND");
        p.put("vnp_TxnRef", txnRef);
        p.put("vnp_OrderInfo","Thanh toan don hang " + orderId);
        p.put("vnp_OrderType","other");
        p.put("vnp_Locale","vn");
        p.put("vnp_IpAddr", clientIp);
        p.put("vnp_ReturnUrl", returnUrl);
        p.put("vnp_CreateDate", LocalDateTime.now().format(FMT));
        p.put("vnp_ExpireDate", LocalDateTime.now().plusMinutes(15).format(FMT));

        String dataToSign = VnpayUtil.buildQuery(p, true);
        String secureHash = VnpayUtil.hmacSHA512(hashSecret, dataToSign);

        return payUrl + "?" + dataToSign + "&vnp_SecureHash=" + secureHash;
    }

    public boolean verifySignature(Map<String,String> params) {
        String secureHash = params.remove("vnp_SecureHash");
        params.remove("vnp_SecureHashType");
        String data = VnpayUtil.buildQuery(params, true);
        log.info("[VNPAY][VERIFY] dataToVerify={}", data);
        String myHash = VnpayUtil.hmacSHA512(hashSecret, data);
        return myHash.equalsIgnoreCase(secureHash);
    }
}
