export async function request(url: string, options: any = {}) {

  const token = localStorage.getItem('accessToken');

  const headers = {
    'Content-Type': 'application/json',
    ...options.headers,
    ...(token ? {
      Authorization: 'Bearer ' + token
    } : {})
  };

  const res = await fetch(url, {
    ...options,
    headers
  });

  const data = await res.json();

  if (data.code === 401) {
    location.href = '/login';
  }

  return data;
}