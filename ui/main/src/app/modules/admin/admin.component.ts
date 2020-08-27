import { Component, OnInit } from '@angular/core';
import { UserService } from '@ofServices/user.service';
import { User } from '@ofModel/user.model';
import { Ng2TableModule, NgTableComponent } from 'ng2-table/ng2-table';

@Component({
  selector: 'of-admin',
  templateUrl: './admin.component.html',
  styleUrls: ['./admin.component.scss']
})
export class AdminComponent implements OnInit {

  activeTab = 'users';

  constructor() {
   }

  ngOnInit() {

  }

  setActiveTab(tab: string): void {
    this.activeTab = tab;
  }

}
