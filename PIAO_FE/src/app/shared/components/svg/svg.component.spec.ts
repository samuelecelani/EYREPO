import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SvgComponent } from './svg.component';

describe('IconComponent', () => {
  let component: SvgComponent;
  let fixture: ComponentFixture<SvgComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [SvgComponent],
    }).compileComponents();

    fixture = TestBed.createComponent(SvgComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('svg', () => {
    component.icon = 'Inbox';
    component.ngOnInit();
    expect(component.iconPath).toEqual('/assets/icon/Inbox.svg');
  });

  it('without svg', () => {
    component.ngOnInit();
    expect(component.iconPath).toEqual('/assets/icon/');
  });
});
