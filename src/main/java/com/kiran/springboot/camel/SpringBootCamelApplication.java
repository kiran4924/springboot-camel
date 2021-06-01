package com.kiran.springboot.camel;

import org.apache.camel.component.servlet.CamelHttpTransportServlet;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class SpringBootCamelApplication {

	@Value("${camel.api.path}")
	private String camelContext;

	public static void main(String[] args) {
		SpringApplication.run(SpringBootCamelApplication.class, args);
	}

	@Bean
	ServletRegistrationBean camelServletRegistrationBean() {
		ServletRegistrationBean servlet = new ServletRegistrationBean
				(new CamelHttpTransportServlet(), camelContext + "/*");
		servlet.setName("CamelServlet");
		return servlet;
	}
}
