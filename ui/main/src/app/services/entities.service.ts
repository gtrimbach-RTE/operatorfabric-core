import { Injectable } from '@angular/core';
import { environment } from '@env/environment';
import { HttpClient } from '@angular/common/http';
import { CrudService } from './crud-service';
import { Observable } from 'rxjs';
import { Entity } from '@ofModel/entity.model';

@Injectable({
  providedIn: 'root'
})
export class EntitiesService implements CrudService {

 readonly entitiesUrl: string;

  /**
   * @constructor
   * @param httpClient - Angular build-in
   */
  constructor(private httpClient: HttpClient) {
    this.entitiesUrl = `${environment.urls.entities}`;
  }

  getAllEntities(): Observable<Entity[]> {
    return this.httpClient.get<Entity[]>(`${this.entitiesUrl}`);
  }



 updateEntity(entityData: Entity): Observable<Entity> {
    return this.httpClient.post<Entity>(`${this.entitiesUrl}`, entityData);
  }


  getAll(): Observable<any[]> {
    return this.getAllEntities();
  }

  update(data: any): Observable<any> {
    return this.updateEntity(data);
  }



}
