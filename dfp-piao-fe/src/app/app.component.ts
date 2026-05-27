import { Component, inject, OnDestroy, OnInit } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { CommonModule } from '@angular/common';
import { SpinnerComponent } from './shared/components/spinner/spinner.component';
import { ToastNotificationComponent } from './shared/components/toast-notification/toast-notification.component';
import { ModalComponent } from './shared/components/modal/modal.component';
import { ModalService } from './shared/services/modal.service';
import { Subject, takeUntil } from 'rxjs';
import { TypeErrorEnum } from './shared/models/enums/type-error.enum';
import { MetadatoDTO } from './shared/models/interfaces/metadato-dto';

@Component({
  selector: 'app-root',
  imports: [
    RouterOutlet,
    CommonModule,
    SpinnerComponent,
    ToastNotificationComponent,
    ModalComponent,
  ],
  template: `
    <div class="min-h-screen flex flex-col">
      <div class="flex-1">
        <main class="flex-1">
          <router-outlet />
        </main>
      </div>
      <piao-toast-notification />
      <piao-spinner />
      @if (modalOpen) {
        <piao-modal
          [isIcon]="true"
          [icon]="iconModal"
          [iconStyle]="iconStyle"
          [open]="modalOpen"
          [showButtonConfirm]="type === TypeErrorEnum.WARNING"
          [showButtonCancel]="type === TypeErrorEnum.WARNING"
          [showButtonClose]="type === TypeErrorEnum.ERROR"
          [fillColorIcon]="fillColorIcon"
          [isBorder]="true"
          (closed)="modalOpen = false"
          (confirm)="confirmDelete()"
        >
          <div class="main-error-warning">
            <p class="title">{{ modalTitle }}</p>
            <p>{{ modalMessage }}</p>
          </div>
        </piao-modal>
      }
    </div>
  `,
})
export class AppComponent implements OnInit, OnDestroy {
  private modalService = inject(ModalService);
  private destroy$ = new Subject<void>();

  fillColorIcon: string = '#cc334d';

  iconStyle: string = 'icon-modal';

  iconModal: string = 'WarningCircle';

  metadato: MetadatoDTO<any>[] = [];

  type!: TypeErrorEnum;

  TypeErrorEnum = TypeErrorEnum;

  modalOpen = false;
  modalTitle = '';
  modalMessage = '';

  ngOnInit(): void {
    this.modalService.onOpenModal$
      .pipe(takeUntil(this.destroy$))
      .subscribe(({ title, message, typeEnum, metadato }) => {
        this.modalTitle = title;
        this.modalMessage = message;
        this.fillColorIcon = typeEnum === TypeErrorEnum.WARNING ? '#CC7A00' : '#cc334d';
        console.log('typeEnum', typeEnum);
        this.metadato = metadato;
        this.type = typeEnum;
        this.modalOpen = true;
      });
  }

  confirmDelete(): void {
    const metadatoSelected = this.metadato[0];
    if (metadatoSelected) {
      this.modalService.emitConfirmAction(metadatoSelected);
    }
    this.modalOpen = false;
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }
}
