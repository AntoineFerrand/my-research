import { Component, signal, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { IncidentDTO, IncidentSearchFilters, PageResponse } from '../../shared/models/incident.model';
import { IncidentService } from '../../shared/services/incident.service';

/**
 * Incident search component.
 * Allows filtering incidents by title, description, severity and owner.
 * Displays results in a table with query time measurement.
 */
@Component({
  selector: 'app-incident-search',
  standalone: true,
  imports: [CommonModule, FormsModule, TranslateModule],
  templateUrl: './incident-search.component.html',
  styleUrls: ['./incident-search.component.scss']
})
export class IncidentSearchComponent {
  private readonly incidentService = inject(IncidentService);
  private readonly translateService = inject(TranslateService);

  readonly filters = signal<IncidentSearchFilters>({
    title: '',
    description: '',
    severity: '',
    owner: '',
    page: 0,
    size: 10,
    sort: 'createdAt',
    direction: 'desc'
  });

  readonly incidents = signal<IncidentDTO[]>([]);

  // Pagination
  readonly totalElements = signal<number>(0);
  readonly totalPages = signal<number>(0);
  readonly currentPage = signal<number>(0);
  readonly pageSize = signal<number>(10);
  readonly hasNext = signal<boolean>(false);
  readonly hasPrevious = signal<boolean>(false);

  readonly isLoading = signal(false);

  // Last query time (in seconds)
  readonly lastQueryTime = signal<number | null>(null);

  readonly errorMessage = signal<string | null>(null);

  /**
   * Launches incident search with current filters.
   * Measures query execution time on frontend side.
   */
  searchIncidents(): void {
    this.isLoading.set(true);
    this.errorMessage.set(null);
    const startTime = performance.now();

    this.incidentService.searchIncidents(this.filters()).subscribe({
      next: (response: PageResponse<IncidentDTO>) => {
        const endTime = performance.now();
        this.lastQueryTime.set((endTime - startTime) / 1000);
        this.incidents.set(response.items);
        this.totalElements.set(response.totalElements);
        this.totalPages.set(response.totalPages);
        this.currentPage.set(response.currentPage);
        this.pageSize.set(response.pageSize);
        this.hasNext.set(response.hasNext);
        this.hasPrevious.set(response.hasPrevious);
        this.isLoading.set(false);
      },
      error: (error) => {
        const endTime = performance.now();
        this.lastQueryTime.set((endTime - startTime) / 1000);
        this.errorMessage.set(`Error during search: ${error.message}`);
        this.incidents.set([]);
        this.isLoading.set(false);
        console.error('Error while searching for incidents:', error);
      }
    });
  }

  /**
   * Resets all filters and results.
   */
  resetFilters(): void {
    this.filters.set({
      title: '',
      description: '',
      severity: '',
      owner: '',
      page: 0,
      size: 10,
      sort: 'createdAt',
      direction: 'desc'
    });
    this.incidents.set([]);
    this.totalElements.set(0);
    this.totalPages.set(0);
    this.currentPage.set(0);
    this.lastQueryTime.set(null);
    this.errorMessage.set(null);
  }

  updateFilter(key: keyof IncidentSearchFilters, value: string | number): void {
    this.filters.update(current => ({ ...current, [key]: value }));
  }

  changePage(page: number): void {
    this.filters.update(current => ({ ...current, page }));
    this.searchIncidents();
  }

  changePageSize(size: number): void {
    this.filters.update(current => ({ ...current, size, page: 0 }));
    this.searchIncidents();
  }

  nextPage(): void {
    if (this.hasNext()) {
      this.changePage(this.currentPage() + 1);
    }
  }

  previousPage(): void {
    if (this.hasPrevious()) {
      this.changePage(this.currentPage() - 1);
    }
  }

  /**
   * Changes the sort field and direction.
   * If the same field is clicked, toggles the direction.
   */
  changeSort(field: string): void {
    this.filters.update(current => {
      if (current.sort === field) {
        // Toggle direction if same field
        return { ...current, direction: current.direction === 'asc' ? 'desc' : 'asc', page: 0 };
      } else {
        // New field, default to ascending
        return { ...current, sort: field, direction: 'asc', page: 0 };
      }
    });
    this.searchIncidents();
  }

  /**
   * Checks if a column is currently sorted.
   */
  isSorted(field: string): boolean {
    return this.filters().sort === field;
  }

  /**
   * Gets the sort direction for a column.
   */
  getSortDirection(field: string): 'asc' | 'desc' | null {
    return this.isSorted(field) ? this.filters().direction! : null;
  }

  /**
   * Formats date according to current language.
   */
  formatDate(dateString: string): string {
    const date = new Date(dateString);
    const currentLang = this.translateService.getCurrentLang() || 'en';
    const locale = currentLang === 'fr' ? 'fr-FR' : 'en-US';
    return date.toLocaleString(locale);
  }
}
