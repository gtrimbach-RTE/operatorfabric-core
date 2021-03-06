/* Copyright (c) 2018-2020, RTE (http://www.rte-france.com)
 * See AUTHORS.txt
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 * This file is part of the OperatorFabric project.
 */



import {Component, OnInit} from '@angular/core';
import {DomSanitizer, SafeUrl} from "@angular/platform-browser";
import {ActivatedRoute} from '@angular/router';
import { ProcessesService } from '@ofServices/processes.service';
import { concatMap, map } from 'rxjs/operators';
import { Observable } from 'rxjs';


@Component({
  selector: 'of-iframedisplay',
  templateUrl: './iframedisplay.component.html',
  styleUrls: ['./iframedisplay.component.scss']
})
export class IframeDisplayComponent implements OnInit {

  public iframeURL: Observable<SafeUrl>;

  constructor(
              private sanitizer: DomSanitizer,
              private route: ActivatedRoute,
              private businessconfigService : ProcessesService
  ) { }

  ngOnInit() {
    this.iframeURL = this.route.paramMap.pipe(concatMap( paramMap =>
      this.businessconfigService.queryMenuEntryURL(paramMap.get("menu_id"),paramMap.get("menu_version"),paramMap.get("menu_entry_id"))
    )).pipe(map( this.sanitizer.bypassSecurityTrustResourceUrl ));
    
  
  }

}
