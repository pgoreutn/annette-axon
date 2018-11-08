import {Component, OnDestroy, OnInit} from '@angular/core';
import {SchemaBackendService} from '../../shared/services/schema-backend.service';
import {SchemaSummary} from '../../shared/services/model';
import {MatTableDataSource} from '@angular/material';
import {SelectionModel} from '@angular/cdk/collections';
import {FormControl} from '@angular/forms';
import {debounceTime} from 'rxjs/operators';
import {select, Store} from '@ngrx/store';
import {Observable} from 'rxjs';
import * as fromSchema from '../../shared/services/schema.reducer';
import {DeleteSchema, FindSchemas} from '@app/bpm/shared/services/schema.actions';


@Component({
  selector: 'axon-schemas',
  templateUrl: './schemas.component.html',
  styleUrls: ['./schemas.component.css']
})
export class SchemasComponent implements OnInit, OnDestroy {

  options = [
      'A',
      'AA',
      'B',
      'BB',
      'C',
      'Option B',
      'Option C',
      'Option D',
  ];

  filter;
  filterControl = new FormControl();
  private filterCtrlSub: any;

  displayedColumns: string[] = ['select', 'id', 'name', 'description', 'notation'];
  schemas =  new MatTableDataSource<SchemaSummary>([]);
  selection = new SelectionModel<SchemaSummary>(true, []);


  constructor(private schemaBackend: SchemaBackendService,
              private store: Store<any>) { }

  ngOnInit() {
    // this.store.dispatch(new FindSchemas({ filter: "" }));
    this.store.pipe(select(fromSchema.selectFilter))
        .subscribe(
            res => {
              console.log(`Filter pipe: ${res}`);
              this.filter = res;
            }
        );
    this.store
        .pipe(select(fromSchema.selectAll))
        .subscribe(
            res => {
              console.log('Schemas pipe:');
              console.log(res);
              this.schemas = new MatTableDataSource<SchemaSummary>(res);
              const ids = res.map(s => s.id);
              this.selection.selected.filter(s => !ids.includes(s.id)).forEach(r => this.selection.toggle(r));
            }
        );

    this.filterCtrlSub = this.filterControl.valueChanges
        .pipe(debounceTime(500))
        .subscribe(newFilter => this.find(newFilter));
  }

  ngOnDestroy() {
    this.filterCtrlSub.unsubscribe();
  }

  /** Whether the number of selected elements matches the total number of rows. */
  isAllSelected() {
    const numSelected = this.selection.selected.length;
    const numRows = this.schemas.data.length;
    return numSelected === numRows;
  }

  /** Selects all rows if they are not all selected; otherwise clear selection. */
  masterToggle() {
    this.isAllSelected() ?
        this.selection.clear() :
        this.schemas.data.forEach(row => this.selection.select(row));
  }

  private find(newFilter: string) {
    console.log(`new filter: ${newFilter}`);
    this.store.dispatch(new FindSchemas({ filter: newFilter }));
  }

  firstSelected() {
    if (this.selection.selected && this.selection.selected[0] && this.selection.selected[0].id) {
      return this.selection.selected[0].id;
    } else {
      return 'none';
    }
  }

  deleteSelected() {
    this.store.dispatch(new DeleteSchema({ id: this.selection.selected[0].id }));

  }

  clearFilter() {
    this.find('');
  }

  refresh() {
    this.find(this.filter);
  }
}
