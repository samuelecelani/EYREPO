import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ElencoPiaoPubblicatiComponent } from './elenco-piao-pubblicati.component';

describe('ElencoPiaoPubblicatiComponent', () => {
  let component: ElencoPiaoPubblicatiComponent;
  let fixture: ComponentFixture<ElencoPiaoPubblicatiComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ElencoPiaoPubblicatiComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ElencoPiaoPubblicatiComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
