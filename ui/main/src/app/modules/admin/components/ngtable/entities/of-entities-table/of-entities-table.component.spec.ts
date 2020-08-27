import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { OfEntitiesTableComponent } from './of-entities-table.component';

describe('OfEntitiesTableComponent', () => {
  let component: OfEntitiesTableComponent;
  let fixture: ComponentFixture<OfEntitiesTableComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ OfEntitiesTableComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(OfEntitiesTableComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
