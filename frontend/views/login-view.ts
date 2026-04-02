import {LitElement, html} from 'lit';
import {customElement, state} from 'lit/decorators.js';
import {Router} from '@vaadin/router';

@customElement('login-view')
export class LoginView extends LitElement {

  @state()
  username = '';

  @state()
  password = '';

  async handleLogin() {
    try {
      const res = await fetch('/api/auth/login', {
        method: 'POST',
        headers: {'Content-Type': 'application/json'},
        body: JSON.stringify({
          username: this.username,
          password: this.password
        })
      });

      const result = await res.json();

      if (result.code !== 200) {
        throw new Error(result.message);
      }

      const {accessToken} = result.data;

      localStorage.setItem('accessToken', accessToken);

      Router.go('/dashboard');

    } catch (e) {
      alert('登录失败');
    }
  }

  render() {
    return html`
      <div>
        <input placeholder="用户名"
          @input=${(e: any) => this.username = e.target.value}>

        <input type="password"
          @input=${(e: any) => this.password = e.target.value}>

        <button @click=${this.handleLogin}>
          登录
        </button>
      </div>
    `;
  }
}