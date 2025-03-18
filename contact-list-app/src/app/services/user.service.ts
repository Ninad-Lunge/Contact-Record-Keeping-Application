import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
// import { environment } from 'src/environments/environment';

@Injectable({
  providedIn: 'root',
})
export class UserService {
  private apiUrl = "http://localhost:8080/api/users/me";

  constructor(private http: HttpClient) {}

  getUserDetails(): Observable<any> {
    return this.http.get<any>(this.apiUrl);
  }

  updateUserProfile(user: any): Observable<any> {
    return this.http.put(this.apiUrl, user);
  }
}