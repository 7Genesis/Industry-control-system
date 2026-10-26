package com.genesis.industry.service;

import com.genesis.industry.dto.ProductionItemDTO;
import com.genesis.industry.dto.ProductionSuggestionDTO;
import com.genesis.industry.entity.Product;
import com.genesis.industry.entity.ProductMaterial;
import com.genesis.industry.entity.RawMaterial;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProductionServiceTest {

    private final ProductionService service = new ProductionService();

    private static RawMaterial material(long id, int stock) {
        RawMaterial rm = new RawMaterial();
        rm.id = id;
        rm.name = "MP" + id;
        rm.stockQuantity = stock;
        return rm;
    }

    private static Product product(long id, String price, Object... materialAndQuantity) {
        Product p = new Product();
        p.id = id;
        p.name = "P" + id;
        p.price = new BigDecimal(price);
        p.materials = new ArrayList<>();
        for (int i = 0; i < materialAndQuantity.length; i += 2) {
            ProductMaterial pm = new ProductMaterial();
            pm.product = p;
            pm.rawMaterial = (RawMaterial) materialAndQuantity[i];
            pm.quantityUsed = (Integer) materialAndQuantity[i + 1];
            p.materials.add(pm);
        }
        return p;
    }

    private ProductionSuggestionDTO run(List<Product> products, List<RawMaterial> materials) {
        return service.calculateProductionSuggestion(products, materials);
    }

    @Test
    void produtoMaisCaroEPriorizadoEConsomeOEstoque() {
        RawMaterial aco = material(1, 10);
        Product barato = product(1, "5.00", aco, 1);
        Product caro = product(2, "20.00", aco, 1);

        // o mais barato vem primeiro na lista, mas o mais caro é processado primeiro
        ProductionSuggestionDTO result = run(List.of(barato, caro), List.of(aco));

        assertEquals(1, result.suggestions.size());
        ProductionItemDTO item = result.suggestions.get(0);
        assertEquals(2L, item.productId);
        assertEquals(10, item.quantity);
        assertEquals(0, new BigDecimal("200.00").compareTo(item.totalValue));
    }

    @Test
    void estoqueVirtualImpedeReutilizarMateriaPrimaJaConsumida() {
        RawMaterial aco = material(1, 10);
        RawMaterial tinta = material(2, 100);
        // caro usa 3 de aço (3 unidades, sobra 1); barato usa 2 de aço e 1 de tinta
        Product caro = product(1, "30.00", aco, 3);
        Product barato = product(2, "10.00", aco, 2, tinta, 1);

        ProductionSuggestionDTO result = run(List.of(barato, caro), List.of(aco, tinta));

        assertEquals(1L, result.suggestions.get(0).productId);
        assertEquals(3, result.suggestions.get(0).quantity);
        // sobrou 1 de aço no estoque virtual: o produto barato precisa de 2 e não é produzido
        assertEquals(1, result.suggestions.size());
        assertEquals(0, new BigDecimal("90.00").compareTo(result.grandTotal));
    }

    @Test
    void estoqueVirtualRestanteAindaAbasteceProdutoMaisBarato() {
        RawMaterial aco = material(1, 10);
        Product caro = product(1, "30.00", aco, 4);
        Product barato = product(2, "10.00", aco, 1);

        ProductionSuggestionDTO result = run(List.of(caro, barato), List.of(aco));

        // caro: 10 / 4 = 2 unidades, consome 8; sobram 2 para o barato
        assertEquals(2, result.suggestions.size());
        assertEquals(2, result.suggestions.get(0).quantity);
        assertEquals(2, result.suggestions.get(1).quantity);
        assertEquals(0, new BigDecimal("80.00").compareTo(result.grandTotal));
    }

    @Test
    void divisaoInteiraNaoGeraUnidadeParcial() {
        RawMaterial aco = material(1, 7);
        Product p = product(1, "10.00", aco, 2);

        ProductionSuggestionDTO result = run(List.of(p), List.of(aco));

        assertEquals(3, result.suggestions.get(0).quantity);
    }

    @Test
    void estoqueMenorQueOConsumoPorUnidadeNaoProduz() {
        RawMaterial aco = material(1, 1);
        Product p = product(1, "10.00", aco, 2);

        ProductionSuggestionDTO result = run(List.of(p), List.of(aco));

        assertTrue(result.suggestions.isEmpty());
        assertEquals(0, BigDecimal.ZERO.compareTo(result.grandTotal));
    }

    @Test
    void menorMateriaPrimaLimitaAQuantidade() {
        RawMaterial aco = material(1, 100);
        RawMaterial tinta = material(2, 5);
        Product p = product(1, "10.00", aco, 1, tinta, 1);

        ProductionSuggestionDTO result = run(List.of(p), List.of(aco, tinta));

        assertEquals(5, result.suggestions.get(0).quantity);
    }

    @Test
    void produtoSemMateriaPrimaFicaForaDaSugestao() {
        Product semMateriais = product(1, "50.00");
        Product materiaisNulos = product(2, "40.00");
        materiaisNulos.materials = null;

        ProductionSuggestionDTO result = run(List.of(semMateriais, materiaisNulos), List.of());

        assertTrue(result.suggestions.isEmpty());
        assertEquals(0, BigDecimal.ZERO.compareTo(result.grandTotal));
    }

    @Test
    void estoqueZeroFicaForaDaSugestao() {
        RawMaterial aco = material(1, 0);
        Product p = product(1, "10.00", aco, 1);

        ProductionSuggestionDTO result = run(List.of(p), List.of(aco));

        assertTrue(result.suggestions.isEmpty());
    }

    @Test
    void quantidadeUsadaZeroFicaForaDaSugestao() {
        RawMaterial aco = material(1, 10);
        Product p = product(1, "10.00", aco, 0);

        ProductionSuggestionDTO result = run(List.of(p), List.of(aco));

        assertTrue(result.suggestions.isEmpty());
    }

    @Test
    void materiaPrimaForaDaListaDeEstoqueFicaForaDaSugestao() {
        RawMaterial desconhecida = material(99, 10);
        Product p = product(1, "10.00", desconhecida, 1);

        ProductionSuggestionDTO result = run(List.of(p), List.of());

        assertTrue(result.suggestions.isEmpty());
    }

    @Test
    void valorTotalUsaBigDecimalSemErroDeArredondamento() {
        RawMaterial aco = material(1, 3);
        RawMaterial tinta = material(2, 3);
        Product a = product(1, "0.10", aco, 1);
        Product b = product(2, "0.20", tinta, 1);

        ProductionSuggestionDTO result = run(List.of(a, b), List.of(aco, tinta));

        // com double, 3 * 0.10 + 3 * 0.20 daria 0.9000000000000001
        assertEquals(new BigDecimal("0.60"), result.suggestions.get(0).totalValue);
        assertEquals(new BigDecimal("0.30"), result.suggestions.get(1).totalValue);
        assertEquals(new BigDecimal("0.90"), result.grandTotal);
    }
}
