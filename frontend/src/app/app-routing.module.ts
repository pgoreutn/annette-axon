import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';

import { SettingsComponent } from './settings';
import {AuthGuard} from '@app/core/auth/auth.guard'

const routes: Routes = [
  {
    path: '',
    redirectTo: 'about',
    pathMatch: 'full'
  },
  {
    path: 'settings',
    component: SettingsComponent,
    data: { title: 'axon.menu.settings' },
    canActivate: [AuthGuard]
  },
  {
    path: 'examples',
    loadChildren: 'app/examples/examples.module#ExamplesModule',
    canActivate: [AuthGuard]
  },
  {
    path: '**',
    redirectTo: 'about'
  }
];

@NgModule({
  // useHash supports github.io demo page, remove in your app
  imports: [
    RouterModule.forRoot(routes, {
      enableTracing: false,
      scrollPositionRestoration: 'enabled'
    })
  ],
  exports: [RouterModule]
})
export class AppRoutingModule {}
