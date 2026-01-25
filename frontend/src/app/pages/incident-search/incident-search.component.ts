import { Component, signal, computed, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { IncidentDTO, IncidentSearchFilters, PageResponse } from '../../shared/models/incident.model';
import { IncidentService } from '../../shared/services/incident.service';

/**
 * Composant de recherche d'incidents.
 * Permet de filtrer les incidents par title, description, severity et owner.
 * Affiche les résultats dans un tableau avec mesure du temps de requête.
 */
@Component({
  selector: 'app-incident-search',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './incident-search.component.html',
  styleUrls: ['./incident-search.component.scss']
})
export class IncidentSearchComponent {
  // Injection de service avec inject()
  private readonly incidentService = inject(IncidentService);

  // Filtres de recherche avec signals
  readonly filters = signal<IncidentSearchFilters>({
    title: '',
    description: '',
    severity: '',
    owner: '',
    page: 0,
    size: 10
  });

  // Résultats de la recherche
  readonly incidents = signal<IncidentDTO[]>([]);

  // Métadonnées de pagination
  readonly totalElements = signal<number>(0);
  readonly totalPages = signal<number>(0);
  readonly currentPage = signal<number>(0);
  readonly pageSize = signal<number>(10);
  readonly hasNext = signal<boolean>(false);
  readonly hasPrevious = signal<boolean>(false);

  // État de chargement
  readonly isLoading = signal(false);

  // Temps de la dernière requête (en secondes)
  readonly lastQueryTime = signal<number | null>(null);

  // Message d'erreur
  readonly errorMessage = signal<string | null>(null);

  /**
   * Lance la recherche d'incidents avec les filtres actuels.
   * Mesure le temps d'exécution de la requête côté frontend.
   */
  searchIncidents(): void {
    this.isLoading.set(true);
    this.errorMessage.set(null);
    const startTime = performance.now();

    this.incidentService.searchIncidents(this.filters()).subscribe({
      next: (response: PageResponse<IncidentDTO>) => {
        const endTime = performance.now();
        this.lastQueryTime.set((endTime - startTime) / 1000); // Convertir en secondes
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
   * Réinitialise tous les filtres et les résultats.
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

  /**
   * Met à jour un filtre spécifique.
   */
  updateFilter(key: keyof IncidentSearchFilters, value: string | number): void {
    this.filters.update(current => ({ ...current, [key]: value }));
  }

  /**
   * Change la page courante.
   */
  changePage(page: number): void {
    this.filters.update(current => ({ ...current, page }));
    this.searchIncidents();
  }

  /**
   * Change la taille de page.
   */
  changePageSize(size: number): void {
    this.filters.update(current => ({ ...current, size, page: 0 }));
    this.searchIncidents();
  }

  /**
   * Page suivante.
   */
  nextPage(): void {
    if (this.hasNext()) {
      this.changePage(this.currentPage() + 1);
    }
  }

  /**
   * Page précédente.
   */
  previousPage(): void {
    if (this.hasPrevious()) {
      this.changePage(this.currentPage() - 1);
    }
  }

  /**
   * Formate la date au format français.
   */
  formatDate(dateString: string): string {
    const date = new Date(dateString);
    return date.toLocaleString('fr-FR');
  }
}
