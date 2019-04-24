package com.example.demo.nacos.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * heartbeat
 *
 * @author longqiang
 */
@RestController
public class HeartBeatController {

	@GetMapping("/heartbeat")
	public String heartbeat() {
		return "heartbeat";
	}

}
