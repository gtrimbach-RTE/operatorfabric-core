/* Copyright (c) 2018, RTE (http://www.rte-france.com)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

import {BrowserModule} from '@angular/platform-browser';
import {NgModule} from '@angular/core';
import {BrowserAnimationsModule} from '@angular/platform-browser/animations';
import {HttpClientModule} from '@angular/common/http';
import {StateModule} from '@ofStore/state.module';
import {NgbModule} from '@ng-bootstrap/ng-bootstrap';
import {FeedModule} from './modules/feed/feed.module';
import {AppRoutingModule} from './app-routing.module';
import {ServicesModule} from '@ofServices/services.module';
import {AppComponent} from './app.component';
import {NavbarComponent} from './components/navbar/navbar.component';
import {ArchivesModule} from "./modules/archives/archives.module";
import {FormsModule, ReactiveFormsModule} from "@angular/forms";
import {LoginComponent} from "./components/login/login.component";
import { IconComponent } from './components/icon/icon.component';
import {CommonModule} from "@angular/common";

@NgModule({
    imports: [
        CommonModule,
        BrowserModule,
        FormsModule,
        ReactiveFormsModule,
        BrowserAnimationsModule,
        FeedModule,
        ArchivesModule,
        AppRoutingModule,
        HttpClientModule,
        StateModule.forRoot(),
        ServicesModule.forRoot(),
        NgbModule.forRoot(),
    ],
    declarations: [AppComponent, NavbarComponent, LoginComponent, IconComponent],
    bootstrap: [AppComponent]
})
export class AppModule {
}
