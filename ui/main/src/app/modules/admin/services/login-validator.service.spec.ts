import { TestBed } from '@angular/core/testing';

import { LoginValidatorService } from './login-validator.service';
import { UserService } from '@ofServices/user.service';
import { HttpClientModule } from '@angular/common/http';

describe('LoginValidatorService', () => {

  beforeEach(() => TestBed.configureTestingModule({
    providers: [UserService],
    imports: [
      HttpClientModule
    ]

  }));


  it('should be created', () => {
    const service: LoginValidatorService = TestBed.get(LoginValidatorService);
    expect(service).toBeTruthy();
  });
});
