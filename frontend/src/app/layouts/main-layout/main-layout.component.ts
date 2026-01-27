import { Component, signal, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { TranslateModule, TranslateService } from '@ngx-translate/core';

/**
 * Principal layout component.
 * Contains header, footer and router outlet.
 */
@Component({
  selector: 'app-main-layout',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    TranslateModule
  ],
  templateUrl: './main-layout.component.html',
  styleUrls: ['./main-layout.component.scss']
})
export class MainLayoutComponent {
  private readonly translateService = inject(TranslateService);
  
  readonly currentYear = new Date().getFullYear();
  
  // Current language - loaded from localStorage
  readonly currentLang = signal<string>(this.getStoredLanguage());
  
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
   * Changes application language and saves it in local storage.
   */
  switchLanguage(lang: string): void {
    this.translateService.use(lang);
    this.currentLang.set(lang);
    localStorage.setItem('userLanguage', lang);
  }
}
