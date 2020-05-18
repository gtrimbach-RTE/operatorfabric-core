/* Copyright (c) 2020, RTE (http://www.rte-france.com)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */


import {Component, OnInit} from '@angular/core';
import {DomSanitizer, SafeUrl} from "@angular/platform-browser";
import {ActivatedRoute} from '@angular/router';
import { ThirdsService } from '@ofServices/thirds.service';


@Component({
  selector: 'of-iframedisplay',
  templateUrl: './iframedisplay.component.html',
  styleUrls: ['./iframedisplay.component.scss']
})
export class IframeDisplayComponent implements OnInit {

  public iframeURL: SafeUrl;

  constructor(
              private sanitizer: DomSanitizer,
              private route: ActivatedRoute,
              private thirdService : ThirdsService
  ) { }

  ngOnInit() {
    this.route.paramMap.subscribe( paramMap => 
        this.thirdService.queryMenuEntryURL(paramMap.get("menu_id"),paramMap.get("menu_version"),paramMap.get("menu_entry_id"))
          .subscribe( url =>
              this.iframeURL = this.sanitizer.bypassSecurityTrustResourceUrl(url)
          )
    )
  }

}
