import { Component, signal } from '@angular/core';
import { MainLayoutComponent } from './layouts/main-layout/main-layout.component';
import { TranslateService } from '@ngx-translate/core';
import translationsEN from "../public/i18n/en.json";
import translationsFR from "../public/i18n/fr.json";


@Component({
  selector: 'app-root',
  imports: [MainLayoutComponent],
  templateUrl: './app.html',
  styleUrl: './app.scss'
})

export class App {
  constructor(translate: TranslateService) {
    translate.setTranslation('en', translationsEN);
    translate.setTranslation('fr', translationsFR);
    
    // Charger la langue sauvegardée
    const savedLang = localStorage.getItem('userLanguage') || 'en';
    translate.use(savedLang);
  }
  protected readonly title = signal('frontend');
}
