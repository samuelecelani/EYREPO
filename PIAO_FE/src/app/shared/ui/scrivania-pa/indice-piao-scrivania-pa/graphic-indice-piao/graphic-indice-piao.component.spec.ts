import { ComponentFixture, TestBed } from '@angular/core/testing';

import { GraphicIndicePiaoComponent } from './graphic-indice-piao.component';

describe('GraphicIndicePiaoComponent', () => {
  let component: GraphicIndicePiaoComponent;
  let fixture: ComponentFixture<GraphicIndicePiaoComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [GraphicIndicePiaoComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(GraphicIndicePiaoComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
