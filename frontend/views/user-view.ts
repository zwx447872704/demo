import { LitElement, html } from 'lit';
import '@vaadin/grid';
import '@vaadin/grid/vaadin-grid-column';

export class UserView extends LitElement {

  users: any[] = [];

  connectedCallback() {
    super.connectedCallback();
    this.loadUsers();
  }

  async loadUsers() {
    const token = localStorage.getItem('accessToken');

    const res = await fetch('/api/users', {
      headers: {
        'Authorization': 'Bearer ' + token
      }
    });

    const data = await res.json();
    this.users = data.data || [];
    this.requestUpdate();
  }

  render() {
    return html`
      <h2>用户管理</h2>

      <vaadin-grid .items=${this.users} style="height:400px">

        <vaadin-grid-column
          path="id"
          header="ID">
        </vaadin-grid-column>

        <vaadin-grid-column
          path="username"
          header="用户名">
        </vaadin-grid-column>

        <vaadin-grid-column
          path="role"
          header="角色">
        </vaadin-grid-column>

      </vaadin-grid>
    `;
  }
}

customElements.define('user-view', UserView);