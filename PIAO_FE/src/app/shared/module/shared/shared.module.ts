import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { TranslateModule } from '@ngx-translate/core';
import { HasFunzionalitaDirective } from '../../directives/has-funzionalita.directive';
import { HasRuoloDirective } from '../../directives/has-ruolo.directive';

@NgModule({
  declarations: [],
  imports: [CommonModule, TranslateModule, HasFunzionalitaDirective, HasRuoloDirective],
  exports: [CommonModule, TranslateModule, HasFunzionalitaDirective, HasRuoloDirective],
})
export class SharedModule {}
