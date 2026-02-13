import { Component, inject, OnInit } from '@angular/core';
import { SharedModule } from '../../../shared/module/shared/shared.module';
import { BaseComponent } from '../../../shared/components/base/base.component';
import { Router } from '@angular/router';
import { NovitaComponent } from '../novita/novita.component';
import { CardHeaderNovitaPAComponent } from '../../../shared/ui/novita-pa/card-header-novita-pa/card-header-novita-pa.component';

@Component({
    selector: 'piao-novita-page',
    imports: [SharedModule, CardHeaderNovitaPAComponent, NovitaComponent],
    templateUrl: './novita-page.component.html',
    styleUrl: './novita-page.component.scss'
})
export class NovitaPageComponent extends BaseComponent implements OnInit {
  router: Router = inject(Router);

  ngOnInit(): void {}
}
