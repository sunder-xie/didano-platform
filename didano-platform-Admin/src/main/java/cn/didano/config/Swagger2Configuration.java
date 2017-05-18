package cn.didano.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

/**
 * 接口文档模板配置文件
 * @author stephen
 * Created on 2016年12月20日 下午7:12:33 
 */
@Configuration
@EnableSwagger2
public class Swagger2Configuration {
	@Bean
	public Docket buildDocket() {
		System.out.println("----------------------Swagger2Configuration-------------------");
		return new Docket(DocumentationType.SWAGGER_2).apiInfo(buildApiInf()).select()
				.apis(RequestHandlerSelectors.basePackage("cn.didano"))// 要扫描的对内API(Controller)基础包,要扫描的对外API(Controller)基础包
				.paths(PathSelectors.any()).build();
		
	}
	private ApiInfo buildApiInf() {
		return new ApiInfoBuilder().title("Didano VideoAPI文档").contact("didano").version("1.0").build();
	}
}