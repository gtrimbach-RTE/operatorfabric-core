import { BrowserModule } from '@angular/platform-browser';
import { NgModule } from '@angular/core';
import { Ng2TableModule } from 'ng2-table/ng2-table';
import { OfUsersTableComponent } from './components/ngtable/users/ofuserstable.component';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { PaginationModule } from 'ngx-bootstrap/pagination';
import { OfTableComponent } from './components/ngtable/oftable/oftable.component';
import { CommonModule } from '@angular/common';
import { OfGroupsTableComponent } from './components/ngtable/groups/of-groups-table/of-groups-table.component';
import { OfEntitiesTableComponent } from './components/ngtable/entities/of-entities-table/of-entities-table.component';
import { TranslateModule } from '@ngx-translate/core';

@NgModule({
  declarations: [
    OfUsersTableComponent,
    OfGroupsTableComponent,
    OfEntitiesTableComponent,
    OfTableComponent
  ],
  exports: [
    OfUsersTableComponent,
    OfGroupsTableComponent,
    OfEntitiesTableComponent
  ],
  imports: [
    FormsModule,
    CommonModule,
    Ng2TableModule,
    PaginationModule.forRoot(),
    TranslateModule
  ],
})
export class TableModule {}
