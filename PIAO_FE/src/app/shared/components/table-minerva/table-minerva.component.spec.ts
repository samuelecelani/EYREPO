import { ComponentFixture, TestBed } from '@angular/core/testing';

import { TableMinervaComponent } from './table-minerva.component';

describe('TableMinervaComponent', () => {
  let component: TableMinervaComponent;
  let fixture: ComponentFixture<TableMinervaComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [TableMinervaComponent]
    })
      .compileComponents();

    fixture = TestBed.createComponent(TableMinervaComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
