import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root',
})
export class OrderService {
  private baseUrl = 'http://localhost:2511/orders'; // Replace with actual backend URL

  constructor(private http: HttpClient) {}

  getAllOrders(email: string): Observable<any> {
    return this.http.get(`${this.baseUrl}/all`, { params: { email } });
  }

  searchOrders(email: string, filters: any): Observable<any> {
    return this.http.get(`${this.baseUrl}/search`, { params: { email, ...filters } });
  }
}
