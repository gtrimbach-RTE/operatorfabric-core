import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { AdminComponent } from './admin.component';
import { TableModule } from './table.module';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { AdminRoutingModule } from './admin-rooting.module';
import { PaginationModule } from 'ngx-bootstrap/pagination';
import { CommonModule } from '@angular/common';
import { UserService } from '@ofServices/user.service';
import { HttpClientModule } from '@angular/common/http';
import { DataTableShareService } from './services/data.service';
import { TranslateService, TranslateModule } from '@ngx-translate/core';


describe('AdminComponent', () => {
  let component: AdminComponent;
  let fixture: ComponentFixture<AdminComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [
        AdminComponent
      ],
      providers: [UserService, DataTableShareService],
      imports: [
        FormsModule
        , ReactiveFormsModule
        , TableModule
        , AdminRoutingModule
        , PaginationModule.forRoot()
        , CommonModule
        , HttpClientModule
        , TranslateModule.forRoot(),
      ]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(AdminComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
