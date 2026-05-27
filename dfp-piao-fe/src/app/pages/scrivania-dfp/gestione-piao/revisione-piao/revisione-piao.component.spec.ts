import { ComponentFixture, TestBed } from '@angular/core/testing';

import { RevisionePiaoComponent } from './revisione-piao.component';

describe('RevisionePiaoComponent', () => {
  let component: RevisionePiaoComponent;
  let fixture: ComponentFixture<RevisionePiaoComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [RevisionePiaoComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(RevisionePiaoComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
