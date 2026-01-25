import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { IncidentDTO, IncidentSearchFilters, PageResponse } from '../models/incident.model';
import { environment } from '../../../environments/environment';

/**
 * Service for Incident management
 * Linked to IncidentController
 */
@Injectable({
  providedIn: 'root'
})
export class IncidentService {
  private readonly apiUrl = `${environment.backendUrl}/incidents`;

  constructor(private http: HttpClient) {
  }

  searchIncidents(filters: IncidentSearchFilters): Observable<PageResponse<IncidentDTO>> {
    let params = new HttpParams();

    if (filters.title?.trim())       params = params.set('title', filters.title.trim());
    if (filters.description?.trim()) params = params.set('description', filters.description.trim());
    if (filters.severity?.trim())    params = params.set('severity', filters.severity.trim());
    if (filters.owner?.trim())       params = params.set('owner', filters.owner.trim());
    if (filters.page !== undefined)  params = params.set('page', filters.page.toString());
    if (filters.size !== undefined)  params = params.set('size', filters.size.toString());

    return this.http.get<PageResponse<IncidentDTO>>(this.apiUrl, {params});
  }
}
