package com.sipl.batch.config;

import org.springframework.batch.item.ItemProcessor;

import com.sipl.batch.model.User;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class UserItemProcessor implements ItemProcessor<User, User>{

	@Override
	public User process(User user) throws Exception {
		log.info("in UserItemProcessor , calling process method");
		return user;
	}

}
