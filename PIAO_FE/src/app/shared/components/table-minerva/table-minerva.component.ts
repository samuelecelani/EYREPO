import { Component, Input, OnInit } from '@angular/core';
import { SharedModule } from '../../module/shared/shared.module';

@Component({
    selector: 'piao-table-minerva',
    imports: [SharedModule],
    templateUrl: './table-minerva.component.html',
    styleUrl: './table-minerva.component.scss'
})
export class TableMinervaComponent implements OnInit {

  @Input() data !: any;

  columns !: any;
  rows !: any;

  ngOnInit(): void {
    console.log(this.data);
    this.columns = this.data.columns;
    this.rows = this.data.rows;
  }

}
