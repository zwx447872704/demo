import './views/login-view';
import './views/dashboard-view';
import './views/user-view';
import './views/products-view';
import './layouts/main-layout';

function isLogin() {
  return !!localStorage.getItem('accessToken');
}

// 定义 action 函数的参数类型
interface ActionContext {
  // 如果你需要 context 中有特定字段，可以在这里定义
  pathname?: string;
  search?: string;
  [key: string]: any;
}

interface ActionCommands {
  redirect: (path: string) => void;
}

export const routes = [

  // 登录页
  {
    path: '/login',
    component: 'login-view'
  },

  // 主系统
  {
    path: '/',
    component: 'main-layout',
    action: async (context: ActionContext, commands: ActionCommands) => {
      if (!isLogin()) {
        return commands.redirect('/login');
      }
      return;
    },
    children: [
      { path: '', redirect: '/dashboard' },
      { path: 'dashboard', component: 'dashboard-view' },
      { path: 'users', component: 'user-view' },
      { path: 'products', component: 'products-view' }



    ]
  }
];