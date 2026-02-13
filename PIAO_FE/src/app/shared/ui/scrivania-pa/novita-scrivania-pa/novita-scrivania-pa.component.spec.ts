import { ComponentFixture, TestBed } from '@angular/core/testing';
import { NovitaScrivaniaPAComponent } from './novita-scrivania-pa.component';
import { AlertsService } from '../../../services/alerts.service';
import { Router } from '@angular/router';
import { NovitaComponent } from '../../../../pages/scrivania-pa/novita/novita.component';
import { ButtonComponent } from '../../../components/button/button.component';
import { SharedModule } from '../../../module/shared/shared.module';

describe('NovitaScrivaniaPAComponent', () => {
  let component: NovitaScrivaniaPAComponent;
  let fixture: ComponentFixture<NovitaScrivaniaPAComponent>;
  let routerSpy: any;

  beforeEach(async () => {
    routerSpy = jasmine.createSpyObj('Router', ['navigate']);

    await TestBed.configureTestingModule({
      imports: [NovitaScrivaniaPAComponent, ButtonComponent, SharedModule, NovitaComponent],
      providers: [
        { provide: Router, useValue: routerSpy },
        { provide: AlertsService, useValue: jasmine.createSpyObj('AlertsService', []) },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(NovitaScrivaniaPAComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should navigate to /pages/novita on handleGoToNovita', () => {
    component.handleGoToNovita();
    expect(routerSpy.navigate).toHaveBeenCalledWith(['/pages/novita']);
  });
});
