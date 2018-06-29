package com.jll.canteen.aop;

import com.jll.canteen.service.QueueService;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import java.io.File;

/**
 * ┏━━━━━━━━━Orz━━━━━━━━━┓
 *
 * @author wanghui
 * @date 2018/4/3
 * @e-mail 450193027@qq.com
 * @description doc：
 * ┗━━━━━━━━━Orz━━━━━━━━━┛
 */
@Configuration
@Component
public class MyAdaptor extends WebMvcConfigurerAdapter {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String imgPath = QueueService.getBasePath() + "img" + (QueueService.isWindows ? "\\" : "/");
        File imgDir = new File(imgPath);
        if (!imgDir.exists()) {
            imgDir.mkdirs();
        }
        registry.addResourceHandler("/img/**").addResourceLocations("file:" + imgPath);
        super.addResourceHandlers(registry);
    }
}
