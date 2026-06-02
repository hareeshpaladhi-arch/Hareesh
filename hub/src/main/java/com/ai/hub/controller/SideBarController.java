package com.ai.hub.controller;

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

            SideBar s1 = new SideBar();
            s1.setId(1L);
            s1.setMenuName("Insights");
            s1.setMenuIcon("fa-solid fa-chart-line");
            s1.setMenuUrl("/insights");

            SideBar s2 = new SideBar();
            s2.setId(2L);
            s2.setMenuName("Automation");
            s2.setMenuIcon("fa-solid fa-gear");
            s2.setMenuUrl("/automation");

            dataList = Arrays.asList(s1, s2);

            sideBarRepository.saveAll(dataList);
        }

        return dataList;
    }
}
