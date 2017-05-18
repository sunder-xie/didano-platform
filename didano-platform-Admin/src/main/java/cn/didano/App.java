/*
 * Copyright 2012-2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cn.didano;

import org.apache.log4j.Logger;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

import cn.didano.base.interaction.StorageService;


/**
 * 视频启动程序
 * @TODO 缓存容器没加上，目前只能做到使用spring cache查单个，而且list没有加上，需要更多了解spring cache细节，同时，将来采用redis可能还会大变
 * @TODO 操作记录，一律写日志，不写入库，优化数据库速度，将来采用相关大数据技术做统计
 * @author stephen
 * Created on 2016年12月25日 上午11:48:46 
 */
@SpringBootApplication
@ServletComponentScan
@ComponentScan({"cn.didano"})//@EnableConfigurationProperties({OssInfo.class})
@EnableCaching
public class App {
	static Logger logger = Logger.getLogger(App.class);
	
	public static void main(String[] args) throws Exception {
		SpringApplication.run(App.class, args);
        logger.info("StartVideoApplication Started.............................");
	}
	
	@Bean
	CommandLineRunner init(StorageService storageService) {
		return (args) -> {
            storageService.deleteAll();
            storageService.init();
		};
	}
}
