import { Component, Input, OnInit } from '@angular/core';
import { SharedModule } from '../../module/shared/shared.module';
import { TooltipComponent } from '../tooltip/tooltip.component';

@Component({
  selector: 'piao-table-minerva',
  imports: [SharedModule, TooltipComponent],
  templateUrl: './table-minerva.component.html',
  styleUrl: './table-minerva.component.scss',
})
export class TableMinervaComponent implements OnInit {
  @Input() data!: any;
  @Input() typeTable!: string;

  columns!: Record<string, string>;
  columnKeys!: string[];
  rows!: any;

  ngOnInit(): void {
    console.log(this.data);
    this.columns = this.data.columns;
    this.columnKeys = Object.keys(this.columns);
    this.rows = this.data.rows;
  }
}
