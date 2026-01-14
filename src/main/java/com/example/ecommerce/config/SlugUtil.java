package com.example.ecommerce.config;

import org.springframework.stereotype.Component;

import java.text.Normalizer;

@Component
public class SlugUtil {
    public static String toSlug(String input) {
        return Normalizer.normalize(input, Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}", "")
                .toLowerCase()
                .replaceAll("[^a-z0-9\\s]", "")
                .replaceAll("\\s+", "-");
    }
}
