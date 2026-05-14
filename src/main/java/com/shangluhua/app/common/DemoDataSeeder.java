package com.shangluhua.app.common;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.shangluhua.app.customer.CreateCustomerRequest;
import com.shangluhua.app.customer.CustomerService;
import com.shangluhua.app.auth.AdminUser;
import com.shangluhua.app.auth.AdminUserRepository;
import com.shangluhua.app.inventory.InventoryService;
import com.shangluhua.app.product.CreateProductRequest;
import com.shangluhua.app.product.ProductService;
import com.shangluhua.app.product.ProductSpu;
import com.shangluhua.app.product.ProductSpuRepository;

@Component
public class DemoDataSeeder implements CommandLineRunner {
    private final boolean enabled;
    private final ProductSpuRepository productRepository;
    private final AdminUserRepository adminUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final ProductService productService;
    private final CustomerService customerService;
    private final InventoryService inventoryService;

    public DemoDataSeeder(@Value("${app.seed.enabled:true}") boolean enabled,
                          ProductSpuRepository productRepository,
                          AdminUserRepository adminUserRepository,
                          PasswordEncoder passwordEncoder,
                          ProductService productService,
                          CustomerService customerService,
                          InventoryService inventoryService) {
        this.enabled = enabled;
        this.productRepository = productRepository;
        this.adminUserRepository = adminUserRepository;
        this.passwordEncoder = passwordEncoder;
        this.productService = productService;
        this.customerService = customerService;
        this.inventoryService = inventoryService;
    }

    @Override
    @Transactional
    public void run(String... args) {
        seedAdmin();
        if (!enabled || productRepository.count() > 0) {
            return;
        }
        seedCustomers();
        ProductSpu shirt = productService.create(product("A1628", "米杏短袖宽松衬衫", "衬衫", "夏", "杭州轻织", 89, 45, 26,
                List.of("米白", "杏黄", "栗棕"), List.of("S", "M", "L", "XL")));
        ProductSpu dress = productService.create(product("K3021", "燕麦色收腰通勤裙", "连衣裙", "夏", "南油档口", 168, 89, 52,
                List.of("燕麦", "奶咖", "浅棕"), List.of("S", "M", "L")));
        ProductSpu set = productService.create(product("T8810", "暖棕亚麻两件套", "套装", "春夏", "广州优选", 239, 128, 76,
                List.of("浅驼", "胡桃", "奶咖"), List.of("M", "L", "XL")));
        ProductSpu skirt = productService.create(product("Q5609", "白黄百褶半身裙", "半身裙", "夏", "义乌织造", 118, 62, 34,
                List.of("米黄", "奶白", "焦糖"), List.of("S", "M", "L", "XL")));

        seedInventory(List.of(shirt, dress, set, skirt));
    }

    private void seedAdmin() {
        if (adminUserRepository.findByUsername("admin").isEmpty()) {
            adminUserRepository.save(new AdminUser("admin", passwordEncoder.encode("123456"), "系统管理员"));
        }
    }

    private void seedCustomers() {
        customerService.create(new CreateCustomerRequest("张三服饰", "13800000001", "zhangsan_fushi", "金牌批发客户"));
        customerService.create(new CreateCustomerRequest("南城衣铺", "13800000002", "nancheng_yipu", "批发客户"));
        customerService.create(new CreateCustomerRequest("暖橙女装", "13800000003", "warm_orange", "VIP客户"));
        customerService.create(new CreateCustomerRequest("木棉小店", "13800000004", "mumian_shop", "批发客户"));
    }

    private void seedInventory(List<ProductSpu> products) {
        int amount = 24;
        for (ProductSpu product : products) {
            for (var sku : product.getSkus()) {
                inventoryService.adjust(sku.getId(), "MAIN", amount + (int) (sku.getId() % 31), "DEMO_SEED", product.getId());
            }
            amount += 8;
        }
    }

    private CreateProductRequest product(String code, String name, String category, String season, String supplier,
                                         int retail, int wholesale, int cost, List<String> colors, List<String> sizes) {
        List<CreateProductRequest.SkuSpec> skus = colors.stream()
                .flatMap(color -> sizes.stream().map(size -> new CreateProductRequest.SkuSpec(color, size, code + color + size)))
                .toList();
        return new CreateProductRequest(code, name, category, season, supplier,
                BigDecimal.valueOf(retail), BigDecimal.valueOf(wholesale), BigDecimal.valueOf(cost), skus);
    }
}
