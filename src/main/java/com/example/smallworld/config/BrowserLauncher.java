package com.example.smallworld.config;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.awt.*;
import java.net.URI;

/**
 * 서버가 뜬 뒤 기본 브라우저로 http://localhost:포트 를 자동으로 엽니다.
 * exe 실행 시 "한 번에 앱 화면까지" 보이게 할 수 있습니다.
 */
@Component
public class BrowserLauncher implements ApplicationListener<ApplicationReadyEvent> {

    private final Environment env;

    public BrowserLauncher(Environment env) {
        this.env = env;
    }

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        if (!Boolean.parseBoolean(env.getProperty("smallworld.open-browser", "true"))) {
            return;
        }
        new Thread(() -> {
            try {
                Thread.sleep(1500); // 서버가 완전히 뜰 때까지 잠시 대기
                int port = env.getProperty("server.port", Integer.class, 8080);
                String url = "http://localhost:" + port;
                if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                    Desktop.getDesktop().browse(new URI(url));
                }
            } catch (Exception ignored) {
                // 브라우저 열기 실패 시 무시 (서버는 정상 동작)
            }
        }).start();
    }
}
