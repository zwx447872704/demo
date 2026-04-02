export async function request(url: string, options: any = {}) {

  const accessToken = localStorage.getItem('accessToken');

  const headers = {
    'Content-Type': 'application/json',
    ...options.headers,
    ...(accessToken ? {
      'Authorization': 'Bearer ' + accessToken
    } : {})
  };

  const response = await fetch(url, {
    ...options,
    headers
  });

  const data = await response.json();

  // ❗ token 过期处理
  if (data.code === 401) {
    // 可以在这里做 refreshToken（后面我教你）
    window.location.href = '/login';
  }

  return data;
}