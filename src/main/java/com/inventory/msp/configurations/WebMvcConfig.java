package com.inventory.msp.configurations;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.web.multipart.support.StandardServletMultipartResolver;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Global Web Configuration
 * Ensures proper multipart file upload handling (Excel, Images, etc.)
 * and static resource serving if needed.
 */
@Configuration
@EnableWebMvc
public class WebMvcConfig implements WebMvcConfigurer {

    /**
     * ✅ Multipart Resolver (Recommended)
     * Enables Spring to parse multipart/form-data requests properly.
     * Choose ONE resolver — CommonsMultipartResolver (manual limits)
     * OR StandardServletMultipartResolver (Tomcat-managed).
     */
    @Bean
    public StandardServletMultipartResolver multipartResolver() {
        // Uses Servlet 3.0's built-in multipart support
        return new StandardServletMultipartResolver();
    }

    /**
     * Alternative: CommonsMultipartResolver (use this instead if you want file size limits)
     */
    // @Bean(name = "multipartResolver")
    // public CommonsMultipartResolver commonsMultipartResolver() {
    //     CommonsMultipartResolver resolver = new CommonsMultipartResolver();
    //     resolver.setMaxUploadSize(50 * 1024 * 1024); // 50 MB
    //     resolver.setMaxUploadSizePerFile(10 * 1024 * 1024); // 10 MB per file
    //     resolver.setDefaultEncoding("UTF-8");
    //     return resolver;
    // }

    /**
     * Optional: Static resource handler (for serving uploaded files)
     * — You can skip this if you don't store Excel files on disk.
     */

}
