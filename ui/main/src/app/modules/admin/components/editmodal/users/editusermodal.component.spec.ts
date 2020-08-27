import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { EditUsermodalComponent } from './editusermodal.component';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { HttpClientModule } from '@angular/common/http';
import { TranslateModule } from '@ngx-translate/core';
import { CommonModule } from '@angular/common';
import { UserService } from '@ofServices/user.service';
import { GroupsService } from '@ofServices/groups.service';
import { EntitiesService } from '@ofServices/entities.service';
import { NgbModalModule, NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { LoginValidatorService } from 'app/modules/admin/services/login-validator.service';
import { DataTableShareService } from 'app/modules/admin/services/data.service';

describe('EditmodalComponent', () => {
  let component: EditUsermodalComponent;
  let fixture: ComponentFixture<EditUsermodalComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [
        EditUsermodalComponent
      ],
      providers: [UserService, GroupsService, EntitiesService ,LoginValidatorService, DataTableShareService, NgbActiveModal],
      imports: [
        FormsModule
        , ReactiveFormsModule
        , CommonModule
        , HttpClientModule
        , NgbModalModule.forRoot()
        , TranslateModule.forRoot(),
      ]
    })
      .compileComponents();
  }));


  beforeEach(() => {
    fixture = TestBed.createComponent(EditUsermodalComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
