import { ComponentFixture, TestBed } from '@angular/core/testing';

import { TableValidazioneComponent } from './table-validazione.component';

describe('TableValidazioneComponent', () => {
  let component: TableValidazioneComponent;
  let fixture: ComponentFixture<TableValidazioneComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [TableValidazioneComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(TableValidazioneComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
