import { Component, Input, OnDestroy, OnInit } from '@angular/core';
import { CardComponent } from '../../components/card/card.component';
import { SharedModule } from '../../module/shared/shared.module';
import { BaseComponent } from '../../components/base/base.component';
import { GET_ALL_AVVISI } from '../../utils/funzionalita';

@Component({
  selector: 'piao-card-alert',
  imports: [CardComponent, SharedModule],
  templateUrl: './card-alert.component.html',
  styleUrl: './card-alert.component.scss',
})
export class CardAlertComponent extends BaseComponent implements OnInit, OnDestroy {
  @Input() icon!: string;
  piaoCardHeaderContainerClass: string = 'piao-card-alert-container';
  piaoCardHeaderBodyClass: string = 'piao-card-alert-body';
  piaoCardHeaderTitleClass: string = 'piao-card-alert-title';
  piaoCardHeaderSecondSubTitleClass: string = 'piao-card-alert-secondSubTitle';
  @Input() titleCardHeader: string = 'SCRIVANIA_PA.ALERT.TITLE';
  @Input() subTitleCardHeader: string = 'SCRIVANIA_PA.ALERT.SUB_TITLE';
  @Input() secondSubTitleCardHeader!: string;
  @Input() textHrefCardHeader!: string;
  @Input() href!: string;
  @Input() subTitleClass: string = this.piaoCardHeaderTitleClass;

  ngOnInit(): void {}

  override ngOnDestroy(): void {
    super.ngOnDestroy();
  }
}
