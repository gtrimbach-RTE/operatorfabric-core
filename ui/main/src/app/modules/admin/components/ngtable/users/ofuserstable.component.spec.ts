import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { OfUsersTableComponent } from './ofuserstable.component';

describe('NgtableComponent', () => {
  let component: OfUsersTableComponent;
  let fixture: ComponentFixture<OfUsersTableComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ OfUsersTableComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(OfUsersTableComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  xit('should create', () => {
    expect(component).toBeTruthy();
  });
});
