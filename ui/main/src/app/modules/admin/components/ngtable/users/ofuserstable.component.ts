import { Component, OnInit } from '@angular/core';
import { UserService } from '@ofServices/user.service';
import { EditUsermodalComponent } from '../../editmodal/users/editusermodal.component';
import { DataTableShareService } from '../../../services/data.service';
import { OfTableComponent } from '../oftable/oftable.component';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';
import { TranslateService } from '@ngx-translate/core';

@Component({
  selector: 'of-users-table',
  templateUrl: './ofuserstable.component.html',
  styleUrls: ['./ofuserstable.component.scss'],
})
export class OfUsersTableComponent extends OfTableComponent implements OnInit {

  

  constructor(
    protected crudService: UserService,
    protected modalService: NgbModal,
    protected dataService: DataTableShareService,
    protected translate: TranslateService
  ) {
    super(modalService, dataService);
    this.crudService = crudService;
    this.config.sorting = { columns: this.columns };
    this.columns = [
      {
        title: translate.instant('admin.input.user.login'),
        name: 'login',
        filtering: { filterString: '', placeholder: 'Filter by Login' },
      },
      {
        title: translate.instant('admin.input.user.firstname'),
        name: 'firstName',
        filtering: { filterString: '', placeholder: 'Filter by First Name' },
      },
      {
        title: translate.instant('admin.input.user.lastname'),
        name: 'lastName',
        filtering: { filterString: '', placeholder: 'Filter by Last Name' },
      },
      {
        title: translate.instant('admin.input.user.groups'),
        name: 'groups',
        filtering: { filterString: '', placeholder: 'Filter by Groups' },
      },
      {
        title: translate.instant('admin.input.user.entities'),
        name: 'entities',
        filtering: { filterString: '', placeholder: 'Filter by Entities' },
      },
      { title: translate.instant('admin.input.user.edit'), name: 'edit' }
    ];

  }
  

  onCellClick(data: any): any {
    let column=data.column;
    if(column==='edit'){
    const modalRef = this.modalService.open(EditUsermodalComponent);
    modalRef.componentInstance.user = data['row'];
    }
  }

  createNewItem() {
    const modalRef = this.modalService.open(EditUsermodalComponent);
  }

  subscribeTable(): void {
    this.dataService.getUserEvent().subscribe((user) => {
      if (user) {
        const itemIndex = this.data.findIndex((item) => item.login === user.login);
        if (itemIndex >= 0) {
          this.data[itemIndex] = user;
        } else {
          this.data.push(user);
        }
        this.onChangeTable(this.config);
      }
    });
  }
}
