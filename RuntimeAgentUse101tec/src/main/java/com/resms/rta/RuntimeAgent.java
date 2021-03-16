package com.resms.rta;

import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class RuntimeAgent
{
    public static void main( String[] args )
    {
        //System.setProperty("spring.devtools.restart.enabled", "false");
        //SpringApplication.run(App.class,args);
        System.setProperty("zookeeper.sasl.client","false");
        SpringApplication app = new SpringApplication(RuntimeAgent.class);
        app.setBannerMode(Banner.Mode.OFF);
        app.run(args);

//        new SpringApplicationBuilder()
//                .sources(App.class)
//                .bannerMode(Banner.Mode.OFF)
//                .run(args);
    }
}