import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { OfGroupsTableComponent } from './of-groups-table.component';

describe('OfGroupsTableComponent', () => {
  let component: OfGroupsTableComponent;
  let fixture: ComponentFixture<OfGroupsTableComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ OfGroupsTableComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(OfGroupsTableComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
