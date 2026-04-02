import { LitElement, html } from 'lit';
import '@vaadin/grid';
import '@vaadin/grid/vaadin-grid-column';
import '@vaadin/text-field';
import '@vaadin/button';
import '@vaadin/dialog';
import '@vaadin/form-layout';
import '@vaadin/notification';

export class ProductsView extends LitElement {

  // ❗关闭 shadow DOM（避免 Vaadin 坑）
  createRenderRoot() {
    return this;
  }

  productArray: any[] = [];
  keyword = '';

  dialogOpen = false;
  loading = false;

  current: any = { id: null, name: '', price: '' };

  connectedCallback() {
    super.connectedCallback();
    this.loadData();
  }

  // ================== 查询 ==================
  async loadData() {
    this.loading = true;

    const token = localStorage.getItem('accessToken');

    const res = await fetch(`/api/products?keyword=${this.keyword}`, {
      headers: {
        'Authorization': 'Bearer ' + token
      }
    });

    const data = await res.json();
    this.productArray = data.data || [];

    this.loading = false;
    this.requestUpdate();
  }

  search = () => this.loadData();

  // ================== 新增 ==================
  openAdd = () => {
    this.current = { id: null, name: '', price: '' };
    this.dialogOpen = true;
    this.requestUpdate();
  };

  // ================== 编辑 ==================
  openEdit = (item: any) => {
    this.current = { ...item };
    this.dialogOpen = true;
    this.requestUpdate();
  };

  // ================== 校验 ==================
  validate() {
    if (!this.current.name) {
      this.showMsg('名称不能为空');
      return false;
    }
    if (!this.current.price || isNaN(this.current.price)) {
      this.showMsg('价格必须是数字');
      return false;
    }
    return true;
  }

  // ================== 保存 ==================
  async save() {
    if (!this.validate()) return;

    const token = localStorage.getItem('accessToken');

    const url = this.current.id
      ? `/api/products/${this.current.id}`
      : `/api/products`;

    const method = this.current.id ? 'PUT' : 'POST';

    await fetch(url, {
      method,
      headers: {
        'Content-Type': 'application/json',
        'Authorization': 'Bearer ' + token
      },
      body: JSON.stringify(this.current)
    });

    this.dialogOpen = false;
    this.showMsg('保存成功');
    this.loadData();
  }

  // ================== 删除 ==================
  async delete(id: number) {
    if (!confirm('确定删除吗？')) return;

    const token = localStorage.getItem('accessToken');

    await fetch(`/api/products/${id}`, {
      method: 'DELETE',
      headers: {
        'Authorization': 'Bearer ' + token
      }
    });

    this.showMsg('删除成功');
    this.loadData();
  }

  // ================== 提示 ==================
  showMsg(msg: string) {
    const notification = document.createElement('vaadin-notification');
    notification.renderer = (root: any) => root.textContent = msg;
    notification.opened = true;
    document.body.appendChild(notification);
  }

  // ================== UI ==================
  render() {
    return html`
      <div style="padding:24px; background:#f5f5f5; min-height:100vh">

        <div style="
          background:white;
          padding:20px;
          border-radius:12px;
          box-shadow:0 2px 8px rgba(0,0,0,0.1);
        ">

          <h2>产品管理</h2>

          <!-- 工具栏 -->
          <div style="display:flex; gap:12px; margin-bottom:16px;">
            <vaadin-text-field
              placeholder="按名称查询"
              @input=${(e: any) => this.keyword = e.target.value}>
            </vaadin-text-field>

            <vaadin-button theme="primary" @click=${() => this.search()}>
              查询
            </vaadin-button>

            <vaadin-button theme="success" @click=${() => this.openAdd()}>
              新增
            </vaadin-button>
          </div>

          ${this.loading ? html`<div>加载中...</div>` : ''}

          <!-- 表格 -->
          <vaadin-grid .items=${this.productArray} style="height:400px">

            <vaadin-grid-column path="id" header="ID"></vaadin-grid-column>
            <vaadin-grid-column path="name" header="名称"></vaadin-grid-column>
            <vaadin-grid-column path="price" header="价格"></vaadin-grid-column>

            <!-- 操作列（稳定写法） -->
            <vaadin-grid-column
              header="操作"
              .renderer=${(root: any, _: any, model: any) => {

                root.innerHTML = '';

                const editBtn = document.createElement('button');
                editBtn.textContent = '编辑';
                editBtn.onclick = () => this.openEdit(model.item);

                const delBtn = document.createElement('button');
                delBtn.textContent = '删除';
                delBtn.style.color = 'red';
                delBtn.onclick = () => this.delete(model.item.id);

                root.appendChild(editBtn);
                root.appendChild(delBtn);
              }}>
            </vaadin-grid-column>

          </vaadin-grid>

        </div>

        <!-- ✅ 最关键：不用 renderer，直接用 opened -->
        <vaadin-dialog
          .opened=${this.dialogOpen}
          @opened-changed=${(e: any) => this.dialogOpen = e.detail.value}
        >
        </vaadin-dialog>

        <!-- ✅ Dialog 内容直接写在外面（关键技巧） -->
        ${this.dialogOpen ? html`
          <div style="
            position: fixed;
            top:0;left:0;right:0;bottom:0;
            background: rgba(0,0,0,0.3);
            display:flex;
            align-items:center;
            justify-content:center;
          ">
            <div style="
              background:white;
              padding:20px;
              border-radius:8px;
              width:300px;
            ">
              <h3>${this.current.id ? '编辑' : '新增'}产品</h3>

              <vaadin-form-layout>
                <vaadin-text-field
                  label="名称"
                  .value=${this.current.name || ''}
                  @input=${(e: any) => this.current.name = e.target.value}>
                </vaadin-text-field>

                <vaadin-text-field
                  label="价格"
                  .value=${this.current.price || ''}
                  @input=${(e: any) => this.current.price = e.target.value}>
                </vaadin-text-field>
              </vaadin-form-layout>

              <div style="margin-top:16px; text-align:right;">
                <vaadin-button @click=${() => this.dialogOpen = false}>
                  取消
                </vaadin-button>

                <vaadin-button theme="primary" @click=${() => this.save()}>
                  保存
                </vaadin-button>
              </div>
            </div>
          </div>
        ` : ''}

      </div>
    `;
  }
}

customElements.define('products-view', ProductsView);