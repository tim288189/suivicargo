import { Injectable } from '@angular/core';

const TOKEN_KEY = 'suivicargo.token';
const USER_KEY  = 'suivicargo.user';

export interface StoredUser {
  id: number;
  email: string;
  nom: string;
  prenom: string;
  role: string;
}

@Injectable({ providedIn: 'root' })
export class TokenService {

  getToken(): string | null {
    return localStorage.getItem(TOKEN_KEY);
  }

  setToken(token: string): void {
    localStorage.setItem(TOKEN_KEY, token);
  }

  getUser(): StoredUser | null {
    const raw = localStorage.getItem(USER_KEY);
    return raw ? (JSON.parse(raw) as StoredUser) : null;
  }

  setUser(user: StoredUser): void {
    localStorage.setItem(USER_KEY, JSON.stringify(user));
  }

  clear(): void {
    localStorage.removeItem(TOKEN_KEY);
    localStorage.removeItem(USER_KEY);
  }
}
