import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ElencoCategoriaObiettiviComponent } from './elenco-categoria-obiettivi.component';

describe('ElencoCategoriaObiettiviComponent', () => {
  let component: ElencoCategoriaObiettiviComponent;
  let fixture: ComponentFixture<ElencoCategoriaObiettiviComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ElencoCategoriaObiettiviComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ElencoCategoriaObiettiviComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
