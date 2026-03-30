package com.example.Vaadin;

import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Route;

@Route("")
public class MainView extends VerticalLayout implements BeforeEnterObserver {

    public MainView() {
        add("🏠 首页（登录后才能看到）");
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        String token = (String) event.getUI()
                .getSession()
                .getAttribute("token");

        // ❗ 未登录 → 跳登录页
        if (token == null) {
            event.forwardTo("login");
        }
    }
}