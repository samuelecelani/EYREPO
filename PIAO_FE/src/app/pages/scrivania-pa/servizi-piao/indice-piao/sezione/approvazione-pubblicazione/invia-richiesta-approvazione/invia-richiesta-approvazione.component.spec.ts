import { ComponentFixture, TestBed } from '@angular/core/testing';

import { RichiestaApprovazioneComponent } from './richiesta-approvazione.component';

describe('RichiestaApprovazioneComponent', () => {
  let component: RichiestaApprovazioneComponent;
  let fixture: ComponentFixture<RichiestaApprovazioneComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [RichiestaApprovazioneComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(RichiestaApprovazioneComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
