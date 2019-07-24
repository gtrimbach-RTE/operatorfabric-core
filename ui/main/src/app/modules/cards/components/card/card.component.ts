/* Copyright (c) 2018, RTE (http://www.rte-france.com)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

import {Component, Input, OnInit} from '@angular/core';
import {LightCard} from '@ofModel/light-card.model';
import {Router} from '@angular/router';
import {selectCurrentUrl} from '@ofStore/selectors/router.selectors';
import {Store} from '@ngrx/store';
import {AppState} from '@ofStore/index';
import {map, tap} from "rxjs/operators";
import {buildConfigSelector} from "@ofSelectors/config.selectors";
import {TranslateService} from "@ngx-translate/core";
import {TimeService} from "@ofServices/time.service";
import {Action as ThirdAction} from "@ofModel/thirds.model";
import {Observable} from "rxjs";
import {selectThirdActionFromCard} from "@ofSelectors/third-action.selectors";
import {LoadThirdActions} from "@ofActions/third-action.actions";

@Component({
    selector: 'of-card',
    templateUrl: './card.component.html',
    styleUrls: ['./card.component.scss']
})
export class CardComponent implements OnInit {

    @Input() public open: boolean = false;
    @Input() public lightCard: LightCard;
    currentPath: any;
    private _i18nPrefix: string;
    dateToDisplay: string;
    actions: Observable<Array<[string, ThirdAction]>>;

    /* istanbul ignore next */
    constructor(private router: Router,
                private store: Store<AppState>,
                private translate: TranslateService,
                private time: TimeService,
    ){
    }

    public select() {
        this.store.dispatch(new LoadThirdActions({card:this.lightCard}));
        this.actions=this.store.select(selectThirdActionFromCard(this.lightCard));
        this.router.navigate(['/' + this.currentPath, 'cards', this.lightCard.id]);
    }

    ngOnInit() {
        this._i18nPrefix = this.lightCard.publisher + '.' + this.lightCard.publisherVersion + '.'
        this.store.select(selectCurrentUrl).subscribe(url => {
            if (url)
                this.currentPath = url.split('/')[1];
        });
        this.store.select(buildConfigSelector('feed.card.time.display'))
        // use configuration to compute date
            .pipe(map(config => this.computeDisplayedDates(config, this.lightCard)))
            .subscribe(computedDate => this.dateToDisplay = computedDate);
        this.actions = this.store.select(selectThirdActionFromCard(this.lightCard))
        }

    computeDisplayedDates(config: string, lightCard: LightCard): string {
        switch (config) {
            case 'NONE':
                return '';
            case 'LTTD':
                return this.handleDate(lightCard.lttd);
            case 'PUBLICATION':
                return this.handleDate(lightCard.publishDate);
            case 'BUSINESS_START':
                return this.handleDate(lightCard.startDate);
            default:
                return `${this.handleDate(lightCard.startDate)} - ${this.handleDate(lightCard.endDate)}`
        }
    }

    handleDate(timeStamp: number): string {
        return this.time.formatDateTime(timeStamp);
    }

    get i18nPrefix(): string {
        return this._i18nPrefix;
    }

}
