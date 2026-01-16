import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Router, NavigationEnd } from '@angular/router';
import { BreadcrumbComponent } from './breadcrumb.component';
import { Subject } from 'rxjs';

describe('BreadcrumbComponent', () => {
  let component: BreadcrumbComponent;
  let fixture: ComponentFixture<BreadcrumbComponent>;
  let mockRouter: any;
  let routerEventsSubject: Subject<any>;

  beforeEach(async () => {
    routerEventsSubject = new Subject();

    mockRouter = {
      events: routerEventsSubject.asObservable(),
      url: '/pages/profilo',
      navigate: jasmine.createSpy('navigate'),
    };

    await TestBed.configureTestingModule({
      imports: [BreadcrumbComponent],
      providers: [{ provide: Router, useValue: mockRouter }],
    }).compileComponents();

    fixture = TestBed.createComponent(BreadcrumbComponent);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should generate breadcrumbs on init', () => {
    fixture.detectChanges();
    expect(component.breadcrumbs.length).toBeGreaterThan(0);
  });

  it('should not show breadcrumb for home route', () => {
    mockRouter.url = '/';
    fixture.detectChanges();
    expect(component.breadcrumbs.length).toBe(0);
  });

  it('should navigate when breadcrumb item is clicked', () => {
    fixture.detectChanges();
    const item = { label: 'Test', url: '/test', active: false };
    component.navigateTo(item);
    expect(mockRouter.navigate).toHaveBeenCalledWith(['/test']);
  });

  it('should not navigate when active item is clicked', () => {
    fixture.detectChanges();
    const item = { label: 'Test', url: '/test', active: true };
    component.navigateTo(item);
    expect(mockRouter.navigate).not.toHaveBeenCalled();
  });

  it('should update breadcrumbs on navigation', () => {
    fixture.detectChanges();
    const initialLength = component.breadcrumbs.length;

    mockRouter.url = '/pages/servizi-piao';
    routerEventsSubject.next(new NavigationEnd(1, '/pages/servizi-piao', '/pages/servizi-piao'));

    expect(component.breadcrumbs.length).toBeGreaterThan(0);
  });
});
