package com.genesis.industry.service;

import com.genesis.industry.entity.Product;
import com.genesis.industry.entity.ProductMaterial;
import com.genesis.industry.entity.RawMaterial;
import com.genesis.industry.dto.ProductionItemDTO;
import com.genesis.industry.dto.ProductionSuggestionDTO;

import jakarta.enterprise.context.ApplicationScoped;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@ApplicationScoped
public class ProductionService {

    public ProductionSuggestionDTO calculateProductionSuggestion() {

        // 1️⃣ Buscar todos produtos ordenados por preço DESC
        List<Product> products = Product.<Product>listAll().stream()
                .sorted((p1, p2) -> p2.price.compareTo(p1.price))
                .collect(Collectors.toList());

        // 2️⃣ Copiar estoque atual para mapa virtual
        Map<Long, Integer> virtualStock = new HashMap<>();

       for (RawMaterial rm : RawMaterial.<RawMaterial>listAll()) {
            virtualStock.put(rm.id, rm.stockQuantity);
        }

        List<ProductionItemDTO> suggestions = new ArrayList<>();
        BigDecimal grandTotal = BigDecimal.ZERO;

        // 3️⃣ Para cada produto
        for (Product product : products) {

            if (product.materials == null || product.materials.isEmpty()) {
                continue;
            }

            int maxProduction = Integer.MAX_VALUE;

            // 4️⃣ Calcular quantidade possível
            for (ProductMaterial pm : product.materials) {

                Integer available = virtualStock.get(pm.rawMaterial.id);

                if (available == null || pm.quantityUsed == 0) {
                    maxProduction = 0;
                    break;
                }

                int possible = available / pm.quantityUsed;
                maxProduction = Math.min(maxProduction, possible);
            }

            if (maxProduction > 0) {

                // 5️⃣ Atualizar estoque virtual
                for (ProductMaterial pm : product.materials) {
                    Long rawMaterialId = pm.rawMaterial.id;
                    Integer currentStock = virtualStock.get(rawMaterialId);

                    virtualStock.put(
                            rawMaterialId,
                            currentStock - (pm.quantityUsed * maxProduction)
                    );
                }

                // 6️⃣ Criar DTO
                ProductionItemDTO item = new ProductionItemDTO();
                item.productId = product.id;
                item.productName = product.name;
                item.quantity = maxProduction;
                item.unitPrice = product.price;
                item.totalValue = product.price.multiply(BigDecimal.valueOf(maxProduction));

                suggestions.add(item);

                grandTotal = grandTotal.add(item.totalValue);
            }
        }

        ProductionSuggestionDTO result = new ProductionSuggestionDTO();
        result.suggestions = suggestions;
        result.grandTotal = grandTotal;

        return result;
    }
}