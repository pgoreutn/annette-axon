import { NgModule } from '@angular/core';
import { StoreModule } from '@ngrx/store';
import { EffectsModule } from '@ngrx/effects';
import { TranslateModule, TranslateLoader } from '@ngx-translate/core';
import { TranslateHttpLoader } from '@ngx-translate/http-loader';

import {SharedModule} from '../../shared/index'

import { BpmConfigRoutingModule } from './bpm-config-routing.module';
import {HttpClient} from '@angular/common/http'
import {environment} from '../../../environments/environment'
import { ConfigComponent } from './config/config.component';
import { SchemasComponent } from './schemas/schemas.component';
import { HomeComponent } from './home/home.component';
import {BpmSharedModule} from '@app/bpm/shared/bpm-shared.module';
import { SchemaComponent } from './schema/schema.component'
import {BpmnEditComponent} from '@app/bpm/config/schema/bpmn-view/bpmn-edit.component'
import {EntityDefinitionService} from 'ngrx-data'

@NgModule({
  imports: [
    SharedModule,
    BpmConfigRoutingModule,
    BpmSharedModule,

    TranslateModule.forChild({
      loader: {
        provide: TranslateLoader,
        useFactory: HttpLoaderFactory,
        deps: [HttpClient]
      },
      isolate: true
    })
  ],
  declarations: [
      ConfigComponent,
      SchemasComponent,
      HomeComponent,
      SchemaComponent,
      BpmnEditComponent
  ]
})
export class BpmConfigModule {
  constructor() {}
}

export function HttpLoaderFactory(http: HttpClient) {
  return new TranslateHttpLoader(
      http,
      `${environment.i18nPrefix}/assets/i18n/bpm-config/`,
      '.json'
  );
}
