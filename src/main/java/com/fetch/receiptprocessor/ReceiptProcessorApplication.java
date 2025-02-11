package com.fetch.receiptprocessor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@SpringBootApplication
@RestController
@RequestMapping("/receipts")
public class ReceiptProcessorApplication {
    private final Map<String, Integer> receiptPoints = new ConcurrentHashMap<>();

    public static void main(String[] args) {
        SpringApplication.run(ReceiptProcessorApplication.class, args);
    }

    @PostMapping("/process")
    public Map<String, String> processReceipt(@RequestBody Receipt receipt) {
        String receiptId = UUID.randomUUID().toString();
        int points = calculatePoints(receipt);
        receiptPoints.put(receiptId, points);
        return Collections.singletonMap("id", receiptId);
    }

    @GetMapping("/{id}/points")
    public Map<String, Integer> getPoints(@PathVariable String id) {
        return receiptPoints.containsKey(id)
                ? Collections.singletonMap("points", receiptPoints.get(id))
                : Collections.singletonMap("error", 404);
    }

    private int calculatePoints(Receipt receipt) {
        int points = receipt.getRetailer().replaceAll("\\W", "").length();
        double total = Double.parseDouble(receipt.getTotal());
        if (total == Math.floor(total)) points += 50;
        if (total % 0.25 == 0) points += 25;
        points += (receipt.getItems().size() / 2) * 5;
        points += receipt.getItems().stream()
                    .filter(item -> item.getShortDescription().trim().length() % 3 == 0)
                    .mapToInt(item -> (int) Math.ceil(Double.parseDouble(item.getPrice()) * 0.2))
                    .sum();
        points += (Integer.parseInt(receipt.getPurchaseDate().split("-")[2]) % 2 == 1) ? 6 : 0;
        points += (Integer.parseInt(receipt.getPurchaseTime().split(":")[0]) >= 14 &&
                   Integer.parseInt(receipt.getPurchaseTime().split(":")[0]) < 16) ? 10 : 0;
        return points;
    }
}

class Receipt {
    private String retailer;
    private String purchaseDate;
    private String purchaseTime;
    private String total;
    private List<Item> items;

    // Getters and Setters
}

class Item {
    private String shortDescription;
    private String price;

    // Getters and Setters
}
