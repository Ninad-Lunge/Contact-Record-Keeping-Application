import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root',
})
export class UserService {
  private apiUrl = `${environment.backendUrl}/users/me`;

  constructor(private http: HttpClient) {}

  getUserDetails(): Observable<any> {
    return this.http.get<any>(this.apiUrl);
  }

  updateUserProfile(user: any): Observable<any> {
    return this.http.put(this.apiUrl, user);
  }
}