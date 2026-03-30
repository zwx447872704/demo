import { LitElement, html, css } from 'lit';

class MyView extends LitElement {

  static styles = css`
    h1 {
      color: red;
    }
  `;

  render() {
    return html`<h1>Hello from Lit 🚀</h1>`;
  }
}

customElements.define('my-view', MyView);