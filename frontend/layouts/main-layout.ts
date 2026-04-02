import { LitElement, html, css } from 'lit';
import '@vaadin/app-layout';
import '@vaadin/side-nav';
import '@vaadin/button';

export class MainLayout extends LitElement {

  static styles = css`
    .navbar-right {
      margin-left: auto;
    }
    vaadin-button {
      background-color: white;
      color: #1976d2;
      font-weight: bold;
    }
  `;

  render() {
    return html`
      <vaadin-app-layout>

        <!-- 左侧折叠按钮 + 标题 -->
        <vaadin-drawer-toggle slot="navbar"></vaadin-drawer-toggle>
        <h3 slot="navbar">管理系统</h3>

        <!-- 登出按钮，靠右 -->
        <div class="navbar-right" slot="navbar">
          <vaadin-button @click="${this.logout}">登出</vaadin-button>
        </div>

        <!-- 侧边导航 -->
        <vaadin-side-nav slot="drawer">
          <vaadin-side-nav-item path="/dashboard">
            首页
          </vaadin-side-nav-item>

          <!-- <vaadin-side-nav-item path="/users">
            用户管理
          </vaadin-side-nav-item>-->

           <vaadin-side-nav-item path="/products">
              产品列表
            </vaadin-side-nav-item>


        </vaadin-side-nav>

        <!-- 主内容区域 -->
        <div style="padding:16px">
          <slot></slot>
        </div>

      </vaadin-app-layout>
    `;
  }

  connectedCallback() {
    super.connectedCallback();
    const token = localStorage.getItem('accessToken');
    if (!token) {
      console.log('JWT 不存在或已过期，跳转登录页');
      window.location.href = '/login';
    } else {
      console.log('当前 JWT:', token);
      // 可在这里调用后端接口验证 token
      // fetch('/api/test-jwt', { headers: { Authorization: `Bearer ${token}` }})
      //   .then(res => console.log(res.status));
    }
  }

  logout() {
    // 清除 JWT 并跳转登录页
    localStorage.removeItem('accessToken');
    window.location.href = '/login';
  }
}

customElements.define('main-layout', MainLayout);