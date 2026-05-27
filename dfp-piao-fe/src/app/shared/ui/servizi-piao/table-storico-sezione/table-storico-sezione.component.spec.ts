import { ComponentFixture, TestBed } from '@angular/core/testing';

import { TableStoricoSezioneComponent } from './table-storico-sezione.component';

describe('TableStoricoSezioneComponent', () => {
  let component: TableStoricoSezioneComponent;
  let fixture: ComponentFixture<TableStoricoSezioneComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [TableStoricoSezioneComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(TableStoricoSezioneComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
