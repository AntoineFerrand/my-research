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

  // Current language - loaded from localStorage
  readonly currentLang = signal<string>(this.getStoredLanguage());

  readonly filters = signal<IncidentSearchFilters>({
    title: '',
    description: '',
    severity: '',
    owner: '',
    page: 0,
    size: 10
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
        this.errorMessage.set(`Erreur lors de la recherche: ${error.message}`);
        this.incidents.set([]);
        this.isLoading.set(false);
        console.error('Erreur lors de la recherche d\'incidents:', error);
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
      size: 10
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
   * Retrieves stored language or default language.
   */
  private getStoredLanguage(): string {
    const stored = localStorage.getItem('userLanguage');
    if (stored) {
      this.translateService.use(stored);
      return stored;
    }
    return this.translateService.getCurrentLang() || 'en';
  }

  /**
   * Formats date according to current language.
   */
  formatDate(dateString: string): string {
    const date = new Date(dateString);
    const locale = this.currentLang() === 'fr' ? 'fr-FR' : 'en-US';
    return date.toLocaleString(locale);
  }

  /**
   * Changes application language and saves it in local storage.
   */
  switchLanguage(lang: string): void {
    console.log(lang)
    this.translateService.use(lang);
    this.currentLang.set(lang);
    localStorage.setItem('userLanguage', lang);
  }
}
