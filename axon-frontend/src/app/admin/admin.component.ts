import { Component, OnInit } from '@angular/core';
import {Subject} from 'rxjs'
import {select, Store} from '@ngrx/store'
import {ActivationEnd, Router} from '@angular/router'
import {TitleService} from '@app/core'
import {TranslateService} from '@ngx-translate/core'
import {selectorSettings, SettingsState} from '@app/settings'
import {filter, map, takeUntil} from 'rxjs/operators'

@Component({
  selector: 'axon-admin',
  templateUrl: './admin.component.html',
  styleUrls: ['./admin.component.css']
})
export class AdminComponent implements OnInit {
  private unsubscribe$: Subject<void> = new Subject<void>();


  constructor(
      private store: Store<any>,
      private router: Router,
      private titleService: TitleService,
      private translate: TranslateService
  ) {}

  ngOnInit(): void {
    this.translate.setDefaultLang('en');
    this.subscribeToSettings();
    this.subscribeToRouterEvents();
  }

  ngOnDestroy(): void {
    this.unsubscribe$.next();
    this.unsubscribe$.complete();
  }

  private subscribeToSettings() {
    this.store
        .pipe(select(selectorSettings), takeUntil(this.unsubscribe$))
        .subscribe((settings: SettingsState) =>
            this.translate.use(settings.language)
        );
  }

  private subscribeToRouterEvents() {
    this.titleService.setTitle(
        this.router.routerState.snapshot.root,
        this.translate
    );
    this.router.events
        .pipe(
            filter(event => event instanceof ActivationEnd),
            map((event: ActivationEnd) => event.snapshot),
            takeUntil(this.unsubscribe$)
        )
        .subscribe(snapshot =>
            this.titleService.setTitle(snapshot, this.translate)
        );
  }
}
