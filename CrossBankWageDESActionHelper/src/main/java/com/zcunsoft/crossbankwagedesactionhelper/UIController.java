package com.zcunsoft.crossbankwagedesactionhelper;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class UIController {
	@RequestMapping("/")
	public String index(){
		return "index";
	}
}
