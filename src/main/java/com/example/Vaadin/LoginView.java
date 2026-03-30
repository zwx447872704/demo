package com.example.Vaadin;

import com.example.common.Result;
import com.example.login.dto.LoginRequest;
import com.example.login.service.AuthService;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.login.LoginForm;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import java.util.Map;

@Route("login")
@PageTitle("登录")
public class LoginView extends VerticalLayout {

//    @Autowired
//    private RestTemplate restTemplate;

    private final AuthService authService;


    public LoginView(AuthService authService) {
        this.authService = authService;

        // 页面整体布局
        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);

        // 标题
        H2 title = new H2("demo");

        // 登录表单
        LoginForm loginForm = new LoginForm();
        loginForm.setForgotPasswordButtonVisible(false);

        // 登录逻辑
        loginForm.addLoginListener(event -> {
            String username = event.getUsername();
            String password = event.getPassword();

            String token = login(username, password);

            if (token != null) {
                UI.getCurrent().getSession().setAttribute("token", token);
                UI.getCurrent().navigate("");
            } else {
                loginForm.setError(true);
            }
        });

        // 卡片容器
        Div card = new Div(title, loginForm);
        card.getStyle()
                .set("padding", "30px")
                .set("border-radius", "10px")
                .set("box-shadow", "0 4px 12px rgba(0,0,0,0.1)")
                .set("background", "white")
                .set("width", "300px")
                .set("text-align", "center");

        // 背景颜色
        getStyle().set("background", "#f5f7fa");

        add(card);
    }

    private String login(String username, String password) {

        try {
            LoginRequest request = new LoginRequest();
            request.setUsername(username);
            request.setPassword(password);

            Result<Map<String, String>> result = authService.login(username, password);

            if (result.getCode() != 200) {
                throw new RuntimeException(result.getMessage());
            }

            return result.getData().get("accessToken");

        } catch (Exception e) {
            System.err.println("登录失败：" + e.getMessage());
            return null;
        }
    }
//    private String login(String username, String password) {
//        try {
//            String url = "http://localhost:8080/api/auth/login";
//
//            // 1️⃣ 请求体
//            Map<String, String> body = new HashMap<>();
//            body.put("username", username);
//            body.put("password", password);
//
//            // 2️⃣ 请求头
//            HttpHeaders headers = new HttpHeaders();
//            headers.setContentType(MediaType.APPLICATION_JSON);
//
//            HttpEntity<Map<String, String>> request =
//                    new HttpEntity<>(body, headers);
//
//            // 3️⃣ 发送请求
//            ResponseEntity<Map> response =
//                    restTemplate.postForEntity(url, request, Map.class);
//
//            // 4️⃣ 解析返回
//            if (response.getStatusCode().is2xxSuccessful()) {
//
//                Map responseBody = response.getBody();
//
//                // 👉 对应你的 Result 结构
//                Map data = (Map) responseBody.get("data");
//
//                if (data != null) {
//                    return (String) data.get("accessToken");
//                }
//            }
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//        return null;
//    }
}