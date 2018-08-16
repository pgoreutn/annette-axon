import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';


import { ExamplesComponent } from './examples/examples.component';
import { TodosComponent } from './todos/todos.component';
import { StockMarketComponent } from './stock-market/stock-market.component';
import { ParentComponent } from './theming/parent/parent.component';
import { AuthenticatedComponent } from './authenticated/authenticated.component';
import {AuthGuard} from '@app/core';

const routes: Routes = [
  {
    path: '',
    component: ExamplesComponent,
    children: [
      {
        path: '',
        redirectTo: 'todos',
        pathMatch: 'full'
      },
      {
        path: 'todos',
        component: TodosComponent,
        data: { title: 'axon.examples.menu.todos' },
        canActivate: [AuthGuard]
      },
      {
        path: 'stock-market',
        component: StockMarketComponent,
        data: { title: 'axon.examples.menu.stocks' },
        canActivate: [AuthGuard]
      },
      {
        path: 'theming',
        component: ParentComponent,
        data: { title: 'axon.examples.menu.theming' },
        canActivate: [AuthGuard]
      },
      {
        path: 'authenticated',
        component: AuthenticatedComponent,
        data: { title: 'axon.examples.menu.auth' },
        canActivate: [AuthGuard]
      }
    ]
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class ExamplesRoutingModule {}
