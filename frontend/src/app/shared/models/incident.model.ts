/**
 * Incident and information on his owner.
 */
export interface IncidentDTO {
  id: number;
  title: string;
  description: string;
  severity: 'LOW' | 'MEDIUM' | 'HIGH';
  createdAt: string; // ISO DateTime format

  // Owner embedded attributes
  ownerId: number;
  ownerLastName: string;
  ownerFirstName: string;
  ownerEmail: string;
}

export interface IncidentSearchFilters {
  title?: string;
  description?: string;
  severity?: string;
  owner?: string;
  page?: number;
  size?: number;
  sort?: string;
  direction?: 'asc' | 'desc';
}

export interface PageResponse<T> {
  items: T[];
  totalElements: number;
  totalPages: number;
  currentPage: number;
  pageSize: number;
  hasNext: boolean;
  hasPrevious: boolean;
}
