package com.ai.login.sidebar.controller;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import com.ai.login.DAO.SideBarRepository;
import com.ai.login.DTO.SideBar;

public class sideBarController {
	@Autowired
	private SideBarRepository sideBarRepository;

	@PostMapping("/sidebar/list")
	public List<SideBar> getSideBarDataList() {

	    List<SideBar> dataList = sideBarRepository.findAll();

	    if (dataList == null || dataList.isEmpty()) {

	        SideBar s1 = new SideBar();
	        s1.setMenuName("Insights");
	        s1.setMenuIcon("insights");
	        s1.setMenuUrl("/insights");

	        SideBar s2 = new SideBar();
	        s2.setMenuName("Automation");
	        s2.setMenuIcon("settings");
	        s2.setMenuUrl("/automation");

	        dataList = Arrays.asList(s1, s2);

	        // Save default data into DB
	        sideBarRepository.saveAll(dataList);
	    }

	    return dataList;
	}
}
