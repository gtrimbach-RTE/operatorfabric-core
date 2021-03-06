/* Copyright (c) 2018-2020, RTE (http://www.rte-france.com)
 * See AUTHORS.txt
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 * This file is part of the OperatorFabric project.
 */

import { Injectable } from "@angular/core";
import { environment } from "@env/environment";
import { Observable, Subject } from 'rxjs';
import { Entity, User } from '@ofModel/user.model';
import { UserWithPerimeters } from "@ofModel/userWithPerimeters.model";
import { HttpClient } from "@angular/common/http";
import { CrudService } from "./crud-service";
import { takeUntil } from 'rxjs/internal/operators/takeUntil';

@Injectable()
export class UserService implements CrudService {
  readonly userUrl: string;

  private _userWithPerimeters: UserWithPerimeters;
  private ngUnsubscribe = new Subject<void>();

  /**
   * @constructor
   * @param httpClient - Angular build-in
   */
  constructor(private httpClient: HttpClient) {
    this.userUrl = `${environment.urls.users}`;
  }

  askUserApplicationRegistered(user: string): Observable<User> {
    return this.httpClient.get<User>(`${this.userUrl}/users/${user}`);
  }

  askCreateUser(userData: User): Observable<User> {
    return this.httpClient.put<User>(
      `${this.userUrl}/users/${userData.login}`,
      userData
    );
  }

  currentUserWithPerimeters(): Observable<UserWithPerimeters> {
    return this.httpClient.get<UserWithPerimeters>(
      `${this.userUrl}/CurrentUserWithPerimeters`
    );
  }

  getAllUsers(): Observable<User[]> {
    return this.httpClient.get<User[]>(`${this.userUrl}`);
  }

  getAll(): Observable<User[]> {
    return this.getAllUsers();
  }

  updateUser(userData: User): Observable<User> {
    return this.httpClient.post<User>(`${this.userUrl}`, userData);
  }

  update(userData: User): Observable<User> {
    return this.updateUser(userData);
  }
  queryAllEntities(): Observable<Entity[]> {
    const url = `${this.userUrl}/entities`;
    return this.httpClient.get<Entity[]>(url);

  }
  public loadUserWithPerimetersData(): void {
    this.currentUserWithPerimeters()
      .pipe(takeUntil(this.ngUnsubscribe))
      .subscribe(
        (userWithPerimeters) => {
          if (userWithPerimeters) {
            this._userWithPerimeters = userWithPerimeters;
          }
        }, (error) => console.error(new Date().toISOString(), 'an error occurred', error)
      );
  }

  public getCurrentUserWithPerimeters(): UserWithPerimeters {
    return this._userWithPerimeters;
  }
}
