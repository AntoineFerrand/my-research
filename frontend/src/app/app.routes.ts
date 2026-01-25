import { Routes } from '@angular/router';
import { IncidentSearchComponent } from './pages/incident-search/incident-search.component';

export const routes: Routes = [
  {
    path: '',
    redirectTo: '/incidents',
    pathMatch: 'full'
  },
  {
    path: 'incidents',
    component: IncidentSearchComponent
  },
  {
    path: '**',
    redirectTo: '/incidents'
  }
];
