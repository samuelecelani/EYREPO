import { ComponentFixture, TestBed } from '@angular/core/testing';
import { BodyTableMinervaSezioneComponent } from './body-table-minerva-sezione.component';

describe('BodyTableMinervaSezioneComponent', () => {
  let component: BodyTableMinervaSezioneComponent;
  let fixture: ComponentFixture<BodyTableMinervaSezioneComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [BodyTableMinervaSezioneComponent],
    }).compileComponents();

    fixture = TestBed.createComponent(BodyTableMinervaSezioneComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
