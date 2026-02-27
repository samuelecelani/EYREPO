import { Component } from '@angular/core';
import { CardHeaderValidazioneComponent } from '../../../shared/ui/validazione/card-header-validazione/card-header-validazione.component';
import { TableValidazioneComponent } from 'src/app/shared/ui/validazione/table-validazione/table-validazione.component';
import { BaseComponent } from '../../../shared/components/base/base.component';
import { SHAPE_ICON } from '../../../shared/utils/constants';
import { RouterModule } from '@angular/router';
import { SharedModule } from '../../../shared/module/shared/shared.module';

@Component({
  selector: 'piao-validazione',
  imports: [SharedModule, CardHeaderValidazioneComponent, TableValidazioneComponent],
  templateUrl: './validazione.component.html',
  styleUrl: './validazione.component.scss',
})
export class ValidazioneComponent extends BaseComponent {}
