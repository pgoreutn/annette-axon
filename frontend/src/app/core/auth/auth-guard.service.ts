import { Injectable } from '@angular/core';
import { CanActivate } from '@angular/router';
import { Store, select } from '@ngrx/store';

import { selectorAuth } from './auth.reducer';
import {KeycloakService} from 'keycloak-angular'

@Injectable()
export class AuthGuardService implements CanActivate {
  isAuthenticated = false;

  constructor(private store: Store<any>, private keycloakService: KeycloakService) {
    this.store
      .pipe(select(selectorAuth))
      .subscribe(auth => (this.isAuthenticated = auth.isAuthenticated));
  }
  canActivate(): boolean {
    return this.isAuthenticated;
  }
}
