/* Copyright (c) 2018-2020, RTE (http://www.rte-france.com)
 * See AUTHORS.txt
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 * This file is part of the OperatorFabric project.
 */


import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { MultiFilterComponent } from './multi-filter.component';
import { ReactiveFormsModule, FormsModule } from '@angular/forms';
import { TranslateModule } from '@ngx-translate/core';
import { HttpClientModule } from '@angular/common/http';
import { zip } from 'rxjs';

describe('MultiFilterComponent', () => {
  let component: MultiFilterComponent;
  let fixture: ComponentFixture<MultiFilterComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      imports: [
        ReactiveFormsModule,
        FormsModule,
        HttpClientModule,
        TranslateModule.forRoot()
      ],
      declarations: [ MultiFilterComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(MultiFilterComponent);
    component = fixture.componentInstance;
  });

  it('should create multi-filter component', () => {
    fixture.detectChanges();
    expect(component).toBeTruthy();
  });
  it('should compute correct value/label list : string', (done) => {
    component.values = ['new-value', 'new-value2'];
    component.preparedList = [];
    fixture.detectChanges();
    expect(component.preparedList[0].value).toEqual('new-value');
    expect(component.preparedList[1].value).toEqual('new-value2');
    zip(component.preparedList[0].label, component.preparedList[1].label).subscribe(([l1, l2]) => {
            expect(l2).toEqual('new-value2');
            done();
        });
    });
    it('should compute correct value/label list : string ', (done) => {
        component.values = ['new-value', 'new-value2'];
        component.preparedList = [];
        fixture.detectChanges();
        expect(component.preparedList[0].value).toEqual('new-value');
        expect(component.preparedList[1].value).toEqual('new-value2');
        zip(component.preparedList[0].label, component.preparedList[1].label).subscribe(([l1, l2]) => {
                expect(l1).toEqual('new-value');
                done();
            });
        });
});
