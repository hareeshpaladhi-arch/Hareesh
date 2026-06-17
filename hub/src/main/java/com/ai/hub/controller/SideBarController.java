package com.ai.hub.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ai.hub.dto.SideBar;
import com.ai.hub.repository.SideBarRepository;



@RestController
public class SideBarController {

    @Autowired
    private SideBarRepository sideBarRepository;

    @PostMapping("/sidebar/list")
    public List<SideBar> getSideBarDataList() {

        List<SideBar> dataList = sideBarRepository.findAll();

        if (dataList == null || dataList.isEmpty()) {

            List<SideBar> menus = new ArrayList<>();

            long id = 1L;

            // Dashboard
            menus.add(createMenu(id++, "Dashboard", "fa-solid fa-gauge", "/dashboard", null));

            // Product
            Long productId = id;
            menus.add(createMenu(id++, "Product", "fa-solid fa-box", "/product", null));
            menus.add(createMenu(id++, "Automation", "fa-solid fa-gear", "/product/automation", productId));
            menus.add(createMenu(id++, "Extracted Data", "fa-solid fa-file-import", "/product/extracted", productId));
            menus.add(createMenu(id++, "Reference Data", "fa-solid fa-book", "/product/reference", productId));
            menus.add(createMenu(id++, "Classification", "fa-solid fa-tags", "/product/classification", productId));
            menus.add(createMenu(id++, "Characteristics", "fa-solid fa-list", "/product/characteristics", productId));
            menus.add(createMenu(id++, "Workflow", "fa-solid fa-diagram-project", "/product/workflow", productId));

            // Service
            Long serviceId = id;
            menus.add(createMenu(id++, "Service", "fa-solid fa-briefcase", "/service", null));
            menus.add(createMenu(id++, "Automation", "fa-solid fa-gear", "/service/automation", serviceId));
            menus.add(createMenu(id++, "Extracted Data", "fa-solid fa-file-import", "/service/extracted", serviceId));
            menus.add(createMenu(id++, "Classification", "fa-solid fa-tags", "/service/classification", serviceId));
            menus.add(createMenu(id++, "Attributes", "fa-solid fa-list", "/service/attributes", serviceId));
            menus.add(createMenu(id++, "Workflow", "fa-solid fa-diagram-project", "/service/workflow", serviceId));

            // Asset
            Long assetId = id;
            menus.add(createMenu(id++, "Asset", "fa-solid fa-cubes", "/asset", null));
            menus.add(createMenu(id++, "Automation", "fa-solid fa-gear", "/asset/automation", assetId));
            menus.add(createMenu(id++, "Extracted Data", "fa-solid fa-file-import", "/asset/extracted", assetId));
            menus.add(createMenu(id++, "Classification", "fa-solid fa-tags", "/asset/classification", assetId));
            menus.add(createMenu(id++, "Lifecycle", "fa-solid fa-clock-rotate-left", "/asset/lifecycle", assetId));
            menus.add(createMenu(id++, "Workflow", "fa-solid fa-diagram-project", "/asset/workflow", assetId));

            // Vendor
            Long vendorId = id;
            menus.add(createMenu(id++, "Vendor", "fa-solid fa-building", "/vendor", null));
            menus.add(createMenu(id++, "Automation", "fa-solid fa-gear", "/vendor/automation", vendorId));
            menus.add(createMenu(id++, "Extracted Data", "fa-solid fa-file-import", "/vendor/extracted", vendorId));
            menus.add(createMenu(id++, "Vendor Master", "fa-solid fa-address-card", "/vendor/master", vendorId));
            menus.add(createMenu(id++, "Compliance", "fa-solid fa-shield-halved", "/vendor/compliance", vendorId));
            menus.add(createMenu(id++, "Workflow", "fa-solid fa-diagram-project", "/vendor/workflow", vendorId));

            // Customer
            Long customerId = id;
            menus.add(createMenu(id++, "Customer", "fa-solid fa-users", "/customer", null));
            menus.add(createMenu(id++, "Automation", "fa-solid fa-gear", "/customer/automation", customerId));
            menus.add(createMenu(id++, "Extracted Data", "fa-solid fa-file-import", "/customer/extracted", customerId));
            menus.add(createMenu(id++, "Customer Master", "fa-solid fa-user-group", "/customer/master", customerId));
            menus.add(createMenu(id++, "Hierarchy", "fa-solid fa-sitemap", "/customer/hierarchy", customerId));
            menus.add(createMenu(id++, "Workflow", "fa-solid fa-diagram-project", "/customer/workflow", customerId));

            // Reference Data
            Long refId = id;
            menus.add(createMenu(id++, "Reference Data", "fa-solid fa-book", "/reference", null));
            menus.add(createMenu(id++, "UOM Management", "fa-solid fa-ruler", "/reference/uom", refId));
            menus.add(createMenu(id++, "Currency", "fa-solid fa-dollar-sign", "/reference/currency", refId));
            menus.add(createMenu(id++, "Country", "fa-solid fa-globe", "/reference/country", refId));
            menus.add(createMenu(id++, "Tax Codes", "fa-solid fa-receipt", "/reference/tax", refId));

            // Data Quality
            Long dqId = id;
            menus.add(createMenu(id++, "Data Quality", "fa-solid fa-check-double", "/data-quality", null));
            menus.add(createMenu(id++, "Profiling", "fa-solid fa-chart-column", "/data-quality/profiling", dqId));
            menus.add(createMenu(id++, "Validation Rules", "fa-solid fa-circle-check", "/data-quality/rules", dqId));
            menus.add(createMenu(id++, "Duplicate Detection", "fa-solid fa-copy", "/data-quality/duplicates", dqId));
            menus.add(createMenu(id++, "Standardization", "fa-solid fa-wand-magic-sparkles", "/data-quality/standardization", dqId));

            // Governance
            Long govId = id;
            menus.add(createMenu(id++, "Governance", "fa-solid fa-scale-balanced", "/governance", null));
            menus.add(createMenu(id++, "Business Rules", "fa-solid fa-gavel", "/governance/rules", govId));
            menus.add(createMenu(id++, "Change Requests", "fa-solid fa-code-pull-request", "/governance/change-requests", govId));
            menus.add(createMenu(id++, "Approval Queue", "fa-solid fa-list-check", "/governance/approvals", govId));
            menus.add(createMenu(id++, "Audit Trail", "fa-solid fa-clock-rotate-left", "/governance/audit", govId));

            // Integration
            Long integrationId = id;
            menus.add(createMenu(id++, "Integration", "fa-solid fa-plug", "/integration", null));
            menus.add(createMenu(id++, "Import Data", "fa-solid fa-file-import", "/integration/import", integrationId));
            menus.add(createMenu(id++, "Export Data", "fa-solid fa-file-export", "/integration/export", integrationId));
            menus.add(createMenu(id++, "API Management", "fa-solid fa-cloud", "/integration/api", integrationId));

            // Reports
            Long reportsId = id;
            menus.add(createMenu(id++, "Reports & Analytics", "fa-solid fa-chart-line", "/reports", null));
            menus.add(createMenu(id++, "Dashboard Reports", "fa-solid fa-chart-pie", "/reports/dashboard", reportsId));
            menus.add(createMenu(id++, "Data Quality Reports", "fa-solid fa-chart-bar", "/reports/data-quality", reportsId));
            menus.add(createMenu(id++, "Audit Reports", "fa-solid fa-file-lines", "/reports/audit", reportsId));

            // Administration
            Long adminId = id;
            menus.add(createMenu(id++, "Administration", "fa-solid fa-user-shield", "/admin", null));
            menus.add(createMenu(id++, "Users", "fa-solid fa-users", "/admin/users", adminId));
            menus.add(createMenu(id++, "Roles", "fa-solid fa-user-tag", "/admin/roles", adminId));
            menus.add(createMenu(id++, "Permissions", "fa-solid fa-lock", "/admin/permissions", adminId));
            menus.add(createMenu(id++, "Settings", "fa-solid fa-sliders", "/admin/settings", adminId));

            sideBarRepository.saveAll(menus);

            return menus;
        }

        return dataList;
    }

    private SideBar createMenu(Long id, String menuName, String icon,
                               String url, Long parentId) {

        SideBar menu = new SideBar();
        menu.setId(id);
        menu.setMenuName(menuName);
        menu.setMenuIcon(icon);
        menu.setMenuUrl(url);
        menu.setParentId(parentId);

        return menu;
    }
}
