package com.example.starstar;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.security.config.Customizer.withDefaults;

@SpringBootApplication
@EnableWebSecurity
public class StarstarApplication {
	public static void main(String[] args) {
		SpringApplication.run(StarstarApplication.class, args);
	}

	@RestController
	public static class RootController {
		@GetMapping("/")
		public String root() {
			return
"""
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Root</title>
</head>
<body>
	<h1>Root</h1>
    <a href="/sub/public/a">Public A</a></br>
    <a href="/sub/public/b">Public B</a></br>
    <a href="/sub/private/a">Private A</a></br>
    <a href="/sub/private/b">Private B</a></br>
    <a href="/logout">Logout</a></br>    
</body>
</html>		
""";
		}
	}

	@RestController
	public static class PublicController {

		@GetMapping("/sub/public/a")
		public String publicA() {
			return """
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Root</title>
</head>
<body>
	<h1>/sub/public/a</h1>
    <a href="/">Root</a></br>
    <a href="/sub/public/b">Public B</a></br>
    <a href="/sub/private/a">Private A</a></br>
    <a href="/sub/private/b">Private B</a></br>
    <a href="/logout">Logout</a></br>    
</body>
</html>		
""";
		}

		@GetMapping("/sub/public/b")
		public String publicB() {
			return """
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Root</title>
</head>
<body>
	<h1>/sub/public/b</h1>
    <a href="/">Root</a></br>
    <a href="/sub/public/a">Public A</a></br>
    <a href="/sub/private/a">Private A</a></br>
    <a href="/sub/private/b">Private B</a></br>
	<a href="/logout">Logout</a></br>
</body>
</html>		
""";
		}
	}

	@RestController
	public static class PrivateController {

		@GetMapping("/sub/private/a")
		public String privateA() {
			return """
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Root</title>
</head>
<body>
	<h1>/sub/private/a</h1>
    <a href="/">Root</a></br>
	<a href="/sub/public/a">Public A</a></br>
    <a href="/sub/public/b">Public B</a></br>
    <a href="/sub/private/b">Private B</a></br>
    <a href="/logout">Logout</a></br>
</body>
</html>		
""";
		}

		@GetMapping("/sub/private/b")
		public String privateB() {
			return """
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Root</title>
</head>
<body>
	<h1>/sub/private/b</h1>
    <a href="/">Root</a></br>
    <a href="/sub/public/a">Public A</a></br>
    <a href="/sub/public/b">Public B</a></br>
    <a href="/sub/private/a">Private A</a></br>
    <a href="/logout">Logout</a></br>    
</body>
</html>		
""";
		}
	}

	@Bean
	public SecurityFilterChain loginSecurityFilterChain(HttpSecurity httpSecurity) throws Exception {
		httpSecurity
			// Single FilterChainProxy
			.authorizeHttpRequests(authorizeHttpRequestsConfig -> {
				authorizeHttpRequestsConfig
					// Only the **/private/* require authentication.
					.requestMatchers("**/private/*")
						.fullyAuthenticated()
					.anyRequest().permitAll();
			})
			.formLogin(withDefaults())
				.logout(logoutConfigurer -> {
					logoutConfigurer.logoutSuccessUrl("/");
				});
		return httpSecurity.build();
	}

//	@Bean
//	public SecurityFilterChain loginSecurityFilterChain(HttpSecurity httpSecurity) throws Exception {
//		httpSecurity
//			.securityMatcher("/login", "/logout")
//			.authorizeHttpRequests(authorizeHttpRequestsConfig -> {
//				authorizeHttpRequestsConfig.anyRequest().permitAll();
//			})
//			.formLogin(withDefaults());
//		return httpSecurity.build();
//	}
//
//	@Bean
//	public SecurityFilterChain publicSecurityFilterChain(HttpSecurity httpSecurity) throws Exception {
//		httpSecurity
//			.securityMatcher("/", "/**/public/*")
//			.authorizeHttpRequests(authorizeHttpRequestsConfig -> {
//				authorizeHttpRequestsConfig.anyRequest().permitAll();
//			});
//		return httpSecurity.build();
//	}
//
//	@Bean
//	public SecurityFilterChain privateSecurityFilterChain(HttpSecurity httpSecurity) throws Exception {
//		httpSecurity
//			.securityMatcher("/**/private/*")
//			.authorizeHttpRequests(authorizeHttpRequestsConfig -> {
//				authorizeHttpRequestsConfig
//					.anyRequest()
//						.fullyAuthenticated();
//			})
//			.formLogin(withDefaults());
//		return httpSecurity.build();
//	}
}
