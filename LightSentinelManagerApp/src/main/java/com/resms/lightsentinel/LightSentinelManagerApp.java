package com.resms.lightsentinel;

import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class LightSentinelManagerApp
{
    public static void main( String[] args )
    {
        //System.setProperty("spring.devtools.restart.enabled", "false");
        //SpringApplication.run(App.class,args);
        System.setProperty("zookeeper.sasl.client","false");
        //System.setProperty("jdk.tls.rejectClientInitiatedRenegotiation","true");
        SpringApplication app = new SpringApplication(LightSentinelManagerApp.class);
        app.setBannerMode(Banner.Mode.OFF);
        app.run(args);

//        new SpringApplicationBuilder()
//                .sources(App.class)
//                .bannerMode(Banner.Mode.OFF)
//                .run(args);
    }
}