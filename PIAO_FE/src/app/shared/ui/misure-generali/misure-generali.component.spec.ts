import { ComponentFixture, TestBed } from '@angular/core/testing';
import { MisureGeneraliComponent } from './misure-generali.component';

describe('MisureGeneraliComponent', () => {
  let component: MisureGeneraliComponent;
  let fixture: ComponentFixture<MisureGeneraliComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [MisureGeneraliComponent],
    }).compileComponents();

    fixture = TestBed.createComponent(MisureGeneraliComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
