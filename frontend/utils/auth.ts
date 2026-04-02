export function getToken() {
  return localStorage.getItem('accessToken');
}

export function setToken(token: string) {
  localStorage.setItem('accessToken', token);
}

export function logout() {
  localStorage.clear();
  location.href = '/login';
}