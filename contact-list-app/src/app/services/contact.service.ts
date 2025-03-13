import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class ContactService {
  private apiUrl = 'http://localhost:8080/api/contacts';

  constructor(private http: HttpClient) {}

  getContacts(): Observable<any> {
    return this.http.get(this.apiUrl);
  }

  getContactById(contactId: number): Observable<any> {
    return this.http.get(`${this.apiUrl}/${contactId}`);
  }

  createContact(contact: any): Observable<any> {
    return this.http.post(this.apiUrl, contact);
  }

  updateContact(contactId: number, contact: any): Observable<any> {
    return this.http.put(`${this.apiUrl}/${contactId}`, contact);
  }

  deleteContact(contactId: number): Observable<any> {
    return this.http.delete(`${this.apiUrl}/${contactId}`);
  }

  deleteMultipleContacts(contactIds: number[]): Observable<any> {
    return this.http.post(`${this.apiUrl}/bulk-delete`, contactIds);
  }
}