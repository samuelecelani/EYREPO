import { Component, inject, OnInit, OnDestroy, Input } from '@angular/core';
import { SharedModule } from '../../../module/shared/shared.module';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { UserProfile } from '../../../models/userProfile';
import { ButtonComponent } from '../../../components/button/button.component';
import { TextBoxComponent } from '../../../components/text-box/text-box.component';
import { DropdownComponent } from '../../../components/dropdown/dropdown.component';
import { DatePickerComponent } from '../../../components/date-picker/date-picker.component';
import { ModalComponent } from '../../../components/modal/modal.component';
import { LabelValue } from '../../../models/interfaces/label-value';
import { PIAOService } from '../../../services/piao.service';
import { GestionaleService } from '../../../services/gestionale.service';
import { StrutturaIndicePiaoDTO } from '../../../models/classes/struttura-indice-piao-dto';
import { UtenteRuoloPaDTO } from '../../../models/classes/utente-ruolo-pa-dto';
import { PARiferimentoDTO } from '../../../models/classes/pa-riferimento-dto';
import { ActivatedRoute, Router } from '@angular/router';
import { map, of, catchError, takeUntil, Subject, Observable } from 'rxjs';
import { SvgComponent } from 'src/app/shared/components/svg/svg.component';

type LabelNode = LabelValue & { children?: LabelNode[] };
type RuoloOption = LabelValue & { code: string };
type PaAttivaSession = {
  attiva?: boolean;
  externalId?: string;
  codePA?: string;
  email?: string | null;
  numeroTelefono?: string | null;
  qualifica?: string | null;
  denominazionePA?: string;
};

@Component({
  selector: 'piao-profilo-utente',
  standalone: true,
  imports: [
    SharedModule,
    ButtonComponent,
    ReactiveFormsModule,
    TextBoxComponent,
    DropdownComponent,
    DatePickerComponent,
    ModalComponent,
    SvgComponent,
  ],
  templateUrl: './profilo-utente.component.html',
  styleUrls: ['./profilo-utente.component.scss'],
})
export class ProfiloUtenteComponent implements OnInit, OnDestroy {
  @Input() userId: string | undefined;

  private fb = inject(FormBuilder);
  private piaoService: PIAOService = inject(PIAOService);
  private gestionaleService = inject(GestionaleService);
  private activatedRoute = inject(ActivatedRoute);
  private router = inject(Router);
  private destroy$ = new Subject<void>();

  isEditing = false;
  isNewUser = false;
  showSecondRole = false;
  openAssignConfirmModal = false;
  openRevokeConfirmModal = false;
  private pendingFormValue: any = null;
  private pendingRevokeUserId: string | null = null;
  selectedUser: UtenteRuoloPaDTO | null = null;
  paRiferimento: PARiferimentoDTO | null = null;

  data: UserProfile = {
    profiloUtente: '',
    sezionePiao: '',
    amministrazione: '',
    nome: '',
    cognome: '',
    codiceFiscale: '',
    dataNascita: '',
    luogoNascita: '',
    emailIstituzionale: '',
  };

  // Dynamic options populated from service
  sezionePiaoOptions: LabelNode[] = [];
  private sezionePiaoMap = new Map<string, StrutturaIndicePiaoDTO>(); // mappa label -> DTO originale

  profiloDropdownOptions: RuoloOption[] = [];
  form = this.fb.group({
    profiloUtente: [''],
    codiceRuolo: [''],
    profiloUtenteSecondario: [''],
    codiceRuoloSecondario: [''],
    // usa array per multi-select
    sezionePiao: this.fb.control<string[]>([]),
    sezionePiaoSecondario: this.fb.control<string[]>([]),
    sezionePiaoDisplay: this.fb.control<string>(''), // display-only for read mode
    sezionePiaoSecondarioDisplay: this.fb.control<string>(''), // display-only for read mode (2nd role)
    amministrazione: [''],
    nome: [''],
    cognome: [''],
    codiceFiscale: [''],
    dataNascita: [''],
    luogoNascita: [''],
    emailIstituzionale: [''],
  });

  ngOnInit(): void {
    // Controlla se è un nuovo utente dal path della route
    // Il path "/gestionale/gestione-profilo-utente/new" è statico, non un parametro
    const urlSegments = this.activatedRoute.snapshot.url;
    const isNewFromPath = urlSegments.some((segment) => segment.path === 'new');

    this.isNewUser = isNewFromPath;
    this.isEditing = this.isNewUser;
    console.log(
      'ngOnInit - Is new user:',
      this.isNewUser,
      'urlSegments:',
      urlSegments.map((s) => s.path)
    );

    // Carica le opzioni sezioni PIAO per entrambi i casi (nuovo e modifica)
    this.loadSezionePiaoOptions();
    this.loadRuoliOptions();

    // Recupera PA attiva da sessionStorage per compilare codicePA nel payload
    const paAttiva = this.getPaAttivaFromSessionStorage();
    if (paAttiva?.codePA) {
      this.paRiferimento = {
        attiva: !!paAttiva.attiva,
        externalId: paAttiva.externalId,
        codePA: paAttiva.codePA,
        email: paAttiva.email ?? undefined,
        numeroTelefono: paAttiva.numeroTelefono ?? undefined,
        qualifica: paAttiva.qualifica ?? undefined,
        denominazionePA: paAttiva.denominazionePA || '',
        ruoli: [],
      };
    }

    // Se è un utente esistente con userId, chiama getProfileByExternalId
    if (!this.isNewUser && this.userId) {
      this.gestionaleService
        .getProfileByExternalId(this.userId)
        .pipe(takeUntil(this.destroy$))
        .subscribe({
          next: (utente) => {
            this.selectedUser = utente;
            this.populateFormFromProfile(utente);
          },
          error: (err) => {
            console.error('Error fetching profile by externalId:', err);
          },
        });
    }
  }

  private readonly excludedSezioni = new Set(['0', '5']);
  private readonly secondRoleEnabledValues = new Set(['validatore', 'redattore']);
  private readonly secondRoleDisabledValues = new Set(['coordinatore amministrativo']);

  private getPaAttivaFromSessionStorage(): PaAttivaSession | null {
    try {
      const raw = sessionStorage.getItem('paAttivaDTO');
      if (!raw) {
        return null;
      }
      return JSON.parse(raw) as PaAttivaSession;
    } catch {
      return null;
    }
  }

  private loadRuoliOptions(): void {
    this.gestionaleService
      .getRolesByCodicePa()
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (roles) => {
          this.profiloDropdownOptions = roles
            .map((role) => this.mapRoleToOption(role))
            .filter((role): role is RuoloOption => !!role)
            .filter((role, index, arr) => arr.findIndex((x) => x.code === role.code) === index);
          this.syncRoleControlsWithOptions();
        },
        error: (err) => {
          console.error('Error loading roles by codePA:', err);
          this.profiloDropdownOptions = [];
        },
      });
  }

  private resolveRoleValue(roleName?: string | null): string {
    const normalized = this.normalizeRoleToken(roleName);
    if (!normalized) {
      return '';
    }

    const matched = this.profiloDropdownOptions.find((opt) => {
      const byLabel = this.normalizeRoleToken(String(opt.label)) === normalized;
      const byValue = this.normalizeRoleToken(String(opt.value)) === normalized;
      const byCode = this.normalizeRoleToken(String(opt.code)) === normalized;
      return byLabel || byValue || byCode;
    });

    return matched ? String(matched.value) : String(roleName ?? '');
  }

  private syncRoleControlsWithOptions(): void {
    const firstRole = this.form.controls['profiloUtente'].value;
    const secondRole = this.form.controls['profiloUtenteSecondario'].value;

    const normalizedFirst = this.resolveRoleValue(firstRole);
    const normalizedSecond = this.resolveRoleValue(secondRole);

    if (firstRole && normalizedFirst && normalizedFirst !== firstRole) {
      this.form.controls['profiloUtente'].setValue(normalizedFirst);
    }
    if (secondRole && normalizedSecond && normalizedSecond !== secondRole) {
      this.form.controls['profiloUtenteSecondario'].setValue(normalizedSecond);
    }
  }

  private mapRoleToOption(role: LabelValue): RuoloOption | null {
    const valueObj =
      role?.value && typeof role.value === 'object' && role.value !== null ? role.value : null;

    const code = String(
      role?.additionalField?.codiceRuolo ??
        role?.additionalField?.codice ??
        role?.additionalField?.code ??
        (valueObj as any)?.codiceRuolo ??
        (valueObj as any)?.codice ??
        (valueObj as any)?.code ??
        role?.value ??
        ''
    ).trim();

    const label = String(
      role?.additionalField?.nomeRuolo ??
        role?.additionalField?.descrizione ??
        (valueObj as any)?.nomeRuolo ??
        (valueObj as any)?.descrizione ??
        role?.label ??
        ''
    ).trim();

    if (!code || !label) {
      return null;
    }

    return {
      label,
      value: label,
      code,
    };
  }

  private populateFormFromProfile(utente: UtenteRuoloPaDTO): void {
    // Ruolo dall'array ruoli
    const profiloUtente = this.resolveRoleValue(utente.ruoli?.[0]?.nomeRuolo || '');
    const profiloUtenteSecondario = this.resolveRoleValue(utente.ruoli?.[1]?.nomeRuolo || '');

    // codiceRuolo per ciascun ruolo
    const codiceRuoloPrimario = utente.ruoli?.[0]?.codiceRuolo || '';
    const codiceRuoloSecondario = utente.ruoli?.[1]?.codiceRuolo || '';

    this.form.controls['codiceRuolo'].setValue(codiceRuoloPrimario);
    this.form.controls['codiceRuoloSecondario'].setValue(codiceRuoloSecondario);

    // Amministrazione dal primo codicePA
    const primaryPA = utente.codicePA?.[0];
    const paAttiva = this.getPaAttivaFromSessionStorage();
    const amministrazione =
      primaryPA?.nome || paAttiva?.denominazionePA || this.paRiferimento?.denominazionePA || '';

    // Sezioni dall'utente, separate per ruolo
    const userSezioniPrimario: string[] = [];
    const userSezioniSecondario: string[] = [];
    if (utente.sezioni && Array.isArray(utente.sezioni)) {
      utente.sezioni.forEach((sezione) => {
        if (sezione.strutturaPiao) {
          const numero = this.formatNumeroSezione(sezione.strutturaPiao.numeroSezione);
          const label = [numero, sezione.strutturaPiao.testo?.trim()].filter(Boolean).join(' ');
          if (sezione.codiceRuolo === codiceRuoloPrimario) {
            userSezioniPrimario.push(label);
          }
          if (sezione.codiceRuolo === codiceRuoloSecondario) {
            userSezioniSecondario.push(label);
          }
        }
      });
    }

    this.piaoService
      .getStructureIndicePIAO()
      .pipe(
        map((dtos) => dtos.filter((d) => !this.excludedSezioni.has((d as any).numeroSezione))),
        map((dtos) => this.toLabelNodes(dtos)),
        catchError(() => of<LabelNode[]>([]))
      )
      .subscribe((allOptions) => {
        this.sezionePiaoOptions = allOptions;
        const allSezioni = this.flattenSezioneValues(allOptions);
        const isAllPrimario =
          userSezioniPrimario.length > 0 &&
          this.setEquals(new Set(userSezioniPrimario), new Set(allSezioni));
        const isAllSecondario =
          userSezioniSecondario.length > 0 &&
          this.setEquals(new Set(userSezioniSecondario), new Set(allSezioni));

        // Display: mostra le sezioni per ciascun ruolo
        const displaySezioni = isAllPrimario ? 'Tutte' : userSezioniPrimario.join(', ');
        const displaySezioniSecondario = isAllSecondario
          ? 'Tutte'
          : userSezioniSecondario.join(', ');

        this.data = {
          profiloUtente: [profiloUtente, profiloUtenteSecondario].filter(Boolean).join(', '),
          sezionePiao: displaySezioni,
          amministrazione,
          nome: utente.nome || '',
          cognome: utente.cognome || '',
          codiceFiscale: utente.codiceFiscale || '',
          dataNascita: utente.dataNascita || '',
          luogoNascita: utente.luogoNascita || '',
          emailIstituzionale: utente.email || '',
        };

        this.form.patchValue({
          profiloUtente,
          profiloUtenteSecondario,
          amministrazione: this.data.amministrazione,
          nome: this.data.nome,
          cognome: this.data.cognome,
          codiceFiscale: this.data.codiceFiscale,
          dataNascita: this.data.dataNascita,
          luogoNascita: this.data.luogoNascita,
          emailIstituzionale: this.data.emailIstituzionale,
          sezionePiao: isAllPrimario ? allSezioni : userSezioniPrimario,
          sezionePiaoSecondario: isAllSecondario ? allSezioni : userSezioniSecondario,
          sezionePiaoDisplay: displaySezioni,
          sezionePiaoSecondarioDisplay: displaySezioniSecondario,
        });

        this.form.markAsPristine();
        this.form.markAsUntouched();
        this.showSecondRole = !!profiloUtenteSecondario;
      });
  }

  private loadSezionePiaoOptions(): void {
    this.piaoService
      .getStructureIndicePIAO()
      .pipe(
        map((dtos) => dtos.filter((d) => !this.excludedSezioni.has((d as any).numeroSezione))),
        map((dtos) => this.toLabelNodes(dtos)),
        catchError(() => of<LabelNode[]>([]))
      )
      .subscribe((options) => {
        this.sezionePiaoOptions = options;

        console.log('Sezioni PIAO options loaded:', options);
      });
  }

  private populateFormFromUser(user: UtenteRuoloPaDTO, paRiferimento: PARiferimentoDTO): void {
    // codiceRuolo per ciascun ruolo
    const codiceRuoloPrimario = user.ruoli?.[0]?.codiceRuolo || '';
    const codiceRuoloSecondario = user.ruoli?.[1]?.codiceRuolo || '';

    // Carica tutte le sezioni disponibili dal servizio
    this.piaoService
      .getStructureIndicePIAO()
      .pipe(
        map((dtos) => dtos.filter((d) => !this.excludedSezioni.has((d as any).numeroSezione))),
        map((dtos) => this.toLabelNodes(dtos)),
        catchError(() => of<LabelNode[]>([]))
      )
      .subscribe((allOptions) => {
        this.sezionePiaoOptions = allOptions;

        // Estrai le sezioni dell'utente separate per ruolo
        const userSezioniPrimario: string[] = [];
        const userSezioniSecondario: string[] = [];

        if (user.sezioni && Array.isArray(user.sezioni)) {
          user.sezioni.forEach((sezione) => {
            if (sezione.strutturaPiao) {
              const numero = this.formatNumeroSezione(sezione.strutturaPiao.numeroSezione);
              const label = [numero, sezione.strutturaPiao.testo?.trim()].filter(Boolean).join(' ');
              if (sezione.codiceRuolo === codiceRuoloPrimario) {
                userSezioniPrimario.push(label);
              }
              if (sezione.codiceRuolo === codiceRuoloSecondario) {
                userSezioniSecondario.push(label);
              }
            }
          });
        }

        const allSezioni = this.flattenSezioneValues(allOptions);

        // Confronta per ogni ruolo separatamente
        const isAllPrimario = this.setEquals(new Set(userSezioniPrimario), new Set(allSezioni));
        const isAllSecondario = this.setEquals(new Set(userSezioniSecondario), new Set(allSezioni));

        const displaySezioni = isAllPrimario ? 'Tutte' : userSezioniPrimario.join(', ');
        const displaySezioniSecondario = isAllSecondario
          ? 'Tutte'
          : userSezioniSecondario.join(', ');

        this.data = {
          profiloUtente: (user.ruoli ?? [])
            .map((x) => x.nomeRuolo)
            .filter(Boolean)
            .join(', '),
          sezionePiao: displaySezioni,
          amministrazione: paRiferimento.denominazionePA || '',
          nome: user.nome || '',
          cognome: user.cognome || '',
          codiceFiscale: user.codiceFiscale || '',
          dataNascita: user.dataNascita || '',
          luogoNascita: user.luogoNascita || '',
          emailIstituzionale: user.email || '',
        };

        this.form.patchValue({
          profiloUtente: this.resolveRoleValue(user.ruoli?.[0]?.nomeRuolo || ''),
          profiloUtenteSecondario: this.resolveRoleValue(user.ruoli?.[1]?.nomeRuolo || ''),
          amministrazione: this.data.amministrazione,
          nome: this.data.nome,
          cognome: this.data.cognome,
          codiceFiscale: this.data.codiceFiscale,
          dataNascita: this.data.dataNascita,
          luogoNascita: this.data.luogoNascita,
          emailIstituzionale: this.data.emailIstituzionale,
          sezionePiao: isAllPrimario ? allSezioni : userSezioniPrimario,
          sezionePiaoSecondario: isAllSecondario ? allSezioni : userSezioniSecondario,
          sezionePiaoDisplay: displaySezioni,
          sezionePiaoSecondarioDisplay: displaySezioniSecondario,
        });

        // Reset form state
        this.form.markAsPristine();
        this.form.markAsUntouched();
        this.showSecondRole = !!user.ruoli?.[1]?.nomeRuolo;
      });
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  get v() {
    return this.form.getRawValue();
  }

  // Mostra sempre il valore persistito (incluso 'Tutte') in modalità read-only
  get displaySezionePiao(): string {
    return this.data.sezionePiao ?? '';
  }

  get isReferente(): boolean {
    const codiceRuolo = this.form.controls['codiceRuolo'].value;
    const codiceRuoloSecondario = this.form.controls['codiceRuoloSecondario'].value;
    return codiceRuolo === 'ROLE_REFERENTE' || codiceRuoloSecondario === 'ROLE_REFERENTE';
  }

  private normalizeRole(roleName?: string | null): string {
    return String(roleName ?? '')
      .trim()
      .toLowerCase();
  }

  private normalizeRoleToken(roleName?: string | null): string {
    return this.normalizeRole(roleName)
      .replace(/^role[_\s-]*/i, '')
      .replace(/_/g, ' ')
      .trim();
  }

  get canAddSecondRole(): boolean {
    const selectedRole = this.normalizeRole(this.form.controls['profiloUtente'].value);
    return (
      this.secondRoleEnabledValues.has(selectedRole) &&
      !this.secondRoleDisabledValues.has(selectedRole)
    );
  }

  get secondRoleOptions(): RuoloOption[] {
    const primary = this.normalizeRole(this.form.controls['profiloUtente'].value);
    return this.profiloDropdownOptions.filter((x) => {
      const role = this.normalizeRole(String(x.label));
      // Multi-ruolo consentito solo tra validatore/redattore.
      if (!this.secondRoleEnabledValues.has(role)) {
        return false;
      }
      // Evita duplicazione con il primo ruolo selezionato.
      return role !== primary;
    });
  }

  addSecondRole(): void {
    if (!this.canAddSecondRole) {
      return;
    }
    this.showSecondRole = true;
  }

  removeSecondRole(): void {
    this.showSecondRole = false;
    this.form.controls['profiloUtenteSecondario'].setValue('');
    this.form.controls['sezionePiaoSecondario'].setValue([]);
  }

  handleEditProfile() {
    this.isEditing = true;
    this.piaoService
      .getStructureIndicePIAO() // idPiao optional
      .pipe(
        map((dtos) => dtos.filter((d) => !this.excludedSezioni.has((d as any).numeroSezione))),
        map((value: StrutturaIndicePiaoDTO[]) => this.toLabelNodes(value)),
        catchError(() => of<LabelNode[]>([]))
      )
      .subscribe((options) => {
        // Use only the options from BE; no "Tutte" item added here
        this.sezionePiaoOptions = options;
      });
    // Seed della multi-select da dati esistenti per ogni ruolo
    const currentPrimario = this.data.sezionePiao;
    const currentSecondario = this.form.controls['sezionePiaoSecondarioDisplay'].value || '';

    // Ruolo primario
    if (currentPrimario === 'Tutte') {
      const all = this.flattenSezioneValues(this.sezionePiaoOptions);
      this.form.controls['sezionePiao'].setValue(all);
    } else if (typeof currentPrimario === 'string') {
      const arr = currentPrimario
        .split(',')
        .map((s) => s.trim())
        .filter(Boolean);
      this.form.controls['sezionePiao'].setValue(arr);
    }

    // Ruolo secondario
    if (currentSecondario === 'Tutte') {
      const all = this.flattenSezioneValues(this.sezionePiaoOptions);
      this.form.controls['sezionePiaoSecondario'].setValue(all);
    } else if (typeof currentSecondario === 'string') {
      const arr = currentSecondario
        .split(',')
        .map((s) => s.trim())
        .filter(Boolean);
      this.form.controls['sezionePiaoSecondario'].setValue(arr);
    }

    this.showSecondRole = !!this.form.controls['profiloUtenteSecondario'].value;
    this.syncRoleControlsWithOptions();
  }

  handleCancelEdit(): void {
    if (this.isNewUser) {
      this.router.navigate(['/gestionale']);
      return;
    }

    if (this.selectedUser) {
      this.populateFormFromProfile(this.selectedUser);
    }

    this.pendingFormValue = null;
    this.openAssignConfirmModal = false;
    this.isEditing = false;
  }

  handleChange(event?: any) {
    console.log('Valore cambiato:', event, this.form.getRawValue());

    if (!this.canAddSecondRole) {
      this.removeSecondRole();
      return;
    }

    const firstRole = this.normalizeRole(this.form.controls['profiloUtente'].value);
    const secondRole = this.normalizeRole(this.form.controls['profiloUtenteSecondario'].value);
    if (firstRole && secondRole && firstRole === secondRole) {
      this.form.controls['profiloUtenteSecondario'].setValue('');
    }
  }

  private hasTextValue(value: unknown): boolean {
    return String(value ?? '').trim().length > 0;
  }

  private hasMultiSelection(value: unknown): boolean {
    return Array.isArray(value) && value.length > 0;
  }

  get isSaveProfileButtonDisabled(): boolean {
    if (!this.isEditing) {
      return false;
    }

    const formValue = this.form.getRawValue();

    const mandatoryFieldsFilled = [
      formValue.nome,
      formValue.cognome,
      formValue.codiceFiscale,
      formValue.dataNascita,
      formValue.luogoNascita,
      formValue.emailIstituzionale,
    ].every((field) => this.hasTextValue(field));

    const firstRoleFilled =
      this.hasTextValue(formValue.profiloUtente) && this.hasMultiSelection(formValue.sezionePiao);

    const secondRoleFilled =
      !this.showSecondRole ||
      (this.hasTextValue(formValue.profiloUtenteSecondario) &&
        this.hasMultiSelection(formValue.sezionePiaoSecondario));

    return !(mandatoryFieldsFilled && firstRoleFilled && secondRoleFilled);
  }

  handleSaveButtonClick(): void {
    if (this.isSaveProfileButtonDisabled) {
      return;
    }

    const formValue = this.form.getRawValue();
    this.pendingFormValue = formValue;
    this.openAssignConfirmModal = true;
  }

  closeAssignConfirmModal(): void {
    this.openAssignConfirmModal = false;
    this.pendingFormValue = null;
  }

  confirmAssignProfile(): void {
    if (!this.pendingFormValue) {
      this.closeAssignConfirmModal();
      return;
    }
    this.openAssignConfirmModal = false;
    this.handleSaveProfile(this.pendingFormValue);
    this.pendingFormValue = null;
  }

  handleSaveProfile(precomputedFormValue?: any) {
    const formValue = precomputedFormValue ?? this.form.getRawValue();
    const selected = Array.isArray(formValue.sezionePiao) ? formValue.sezionePiao : [];
    const selectedSecondario = Array.isArray(formValue.sezionePiaoSecondario)
      ? formValue.sezionePiaoSecondario
      : [];
    const all = this.flattenSezioneValues(this.sezionePiaoOptions);
    const isAll = selected.includes('Tutte') || this.setEquals(new Set(selected), new Set(all));
    const isAllSecondario =
      selectedSecondario.includes('Tutte') ||
      this.setEquals(new Set(selectedSecondario), new Set(all));

    const displayPrimario = isAll ? 'Tutte' : selected.join(', ');
    const displaySecondario = isAllSecondario ? 'Tutte' : selectedSecondario.join(', ');

    this.data = {
      ...formValue,
      profiloUtente: [formValue.profiloUtente, formValue.profiloUtenteSecondario]
        .filter(Boolean)
        .join(', '),
      sezionePiao: displayPrimario,
    } as UserProfile;

    // aggiorna i controlli di sola lettura
    this.form.controls['sezionePiaoDisplay'].setValue(displayPrimario);
    this.form.controls['sezionePiaoSecondarioDisplay'].setValue(displaySecondario);

    // Chiama l'API per salvare l'utente
    if (this.isNewUser || this.selectedUser?.id) {
      this.saveUserProfile(formValue);
    } else {
      console.error('No user selected');
    }
  }

  handleRevokeProfile(): void {
    const userId = this.selectedUser?.id;
    if (!userId) {
      return;
    }

    this.pendingRevokeUserId = userId;
    this.openRevokeConfirmModal = true;
  }

  closeRevokeConfirmModal(): void {
    this.openRevokeConfirmModal = false;
    this.pendingRevokeUserId = null;
  }

  confirmRevokeProfile(): void {
    if (!this.pendingRevokeUserId) {
      this.closeRevokeConfirmModal();
      return;
    }

    const userId = this.pendingRevokeUserId;
    this.openRevokeConfirmModal = false;
    this.pendingRevokeUserId = null;
    this.revokeProfile(userId);
  }

  private revokeProfile(userId: string): void {
    this.gestionaleService
      .deleteUtentePa(userId)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: () => {
          this.router.navigate(['/gestionale']);
        },
        error: (err) => {
          console.error('Error revoking user profile:', err);
        },
      });
  }

  private saveUserProfile(formValue: any): void {
    // Estrai le sezioni PIAO selezionate
    const selected = Array.isArray(formValue.sezionePiao) ? formValue.sezionePiao : [];
    const selectedSecondario = Array.isArray(formValue.sezionePiaoSecondario)
      ? formValue.sezionePiaoSecondario
      : [];
    const all = this.flattenSezioneValues(this.sezionePiaoOptions);
    const isAll = selected.includes('Tutte') || this.setEquals(new Set(selected), new Set(all));
    const isAllSecondario =
      selectedSecondario.includes('Tutte') ||
      this.setEquals(new Set(selectedSecondario), new Set(all));

    // Sezioni effettive per ruolo primario e secondario
    const sezioniPrimario = isAll ? all : selected;
    const sezioniSecondario = this.showSecondRole
      ? isAllSecondario
        ? all
        : selectedSecondario
      : [];

    // Usa il valore del ruolo dal form (non da this.data che potrebbe non essere valorizzato per nuovi utenti)
    const profiloUtente = formValue.profiloUtente || '';
    const profiloUtenteSecondario = formValue.profiloUtenteSecondario || '';

    // Risolvi codiceRuolo per ruolo primario e secondario
    const resolveRuoloCode = (roleName: string): string | null => {
      if (!roleName) return null;
      const ruoloOption = this.profiloDropdownOptions.find(
        (opt) => this.normalizeRole(String(opt.label)) === this.normalizeRole(roleName)
      );
      return ruoloOption?.code ?? null;
    };
    const codiceRuoloPrimario = resolveRuoloCode(profiloUtente);
    const codiceRuoloSecondario = this.showSecondRole
      ? resolveRuoloCode(profiloUtenteSecondario)
      : null;

    // Converti le etichette in UtenteRuoliPaSezioneDTO[] con codiceRuolo per ogni riga
    const sezioniToSave: any[] = [];

    // Sezioni per ruolo primario
    if (codiceRuoloPrimario) {
      sezioniPrimario.forEach((label: string) => {
        const originaleDto = this.sezionePiaoMap.get(label);
        if (originaleDto) {
          sezioniToSave.push({
            strutturaPiao: {
              id: originaleDto.id,
              numeroSezione: this.unformatNumeroSezione(originaleDto.numeroSezione),
              testo: originaleDto.testo,
            },
            codiceRuolo: codiceRuoloPrimario,
          });
        }
      });
    }

    // Sezioni per ruolo secondario
    if (codiceRuoloSecondario) {
      sezioniSecondario.forEach((label: string) => {
        const originaleDto = this.sezionePiaoMap.get(label);
        if (originaleDto) {
          sezioniToSave.push({
            strutturaPiao: {
              id: originaleDto.id,
              numeroSezione: this.unformatNumeroSezione(originaleDto.numeroSezione),
              testo: originaleDto.testo,
            },
            codiceRuolo: codiceRuoloSecondario,
          });
        }
      });
    }
    const rolesPayload = [profiloUtente, this.showSecondRole ? profiloUtenteSecondario : '']
      .filter(Boolean)
      .map((roleName) => {
        const ruoloOption = this.profiloDropdownOptions.find(
          (opt) => this.normalizeRole(String(opt.label)) === this.normalizeRole(roleName)
        );
        if (!ruoloOption) {
          return null;
        }
        return {
          codiceRuolo: ruoloOption.code,
          nomeRuolo: String(ruoloOption.label).toLowerCase(),
          tipologia: 'PA',
        };
      })
      .filter(Boolean);

    // Mappa i dati del form a UtenteRuoloPaDTO
    // Per nuovo utente, non includiamo l'ID (il BE lo creerà)
    const paAttiva = this.getPaAttivaFromSessionStorage();

    const utenteToSave: UtenteRuoloPaDTO = {
      ...(this.selectedUser?.id && { id: this.selectedUser.id }),
      nome: formValue.nome,
      cognome: formValue.cognome,
      codiceFiscale: formValue.codiceFiscale,
      dataNascita: formValue.dataNascita,
      luogoNascita: formValue.luogoNascita,
      email: formValue.emailIstituzionale,
      numeroTelefono: undefined,
      ruoli: rolesPayload.length > 0 ? (rolesPayload as any) : undefined,
      // Salva le sezioni
      sezioni: sezioniToSave.length > 0 ? sezioniToSave : undefined,
      // Aggiungi il codicePa dalla PA di riferimento con il nome dell'amministrazione
      ...((paAttiva?.codePA || this.paRiferimento?.codePA) && {
        codicePA: [
          {
            codicePa: paAttiva?.codePA || this.paRiferimento?.codePA,
            nome: paAttiva?.denominazionePA || this.paRiferimento?.denominazionePA || '',
          },
        ],
      }),
    };

    const saveRequest$: Observable<UtenteRuoloPaDTO> = this.isNewUser
      ? this.gestionaleService.saveUtentePa(utenteToSave)
      : this.gestionaleService.updateUtentePa(String(this.selectedUser?.id ?? ''), utenteToSave);

    saveRequest$.pipe(takeUntil(this.destroy$)).subscribe({
      next: (savedUser) => {
        console.log('User profile saved successfully:', savedUser);
        this.isEditing = false;
        this.router.navigate(['/gestionale']);
      },
      error: (err) => {
        console.error('Error saving user profile:', err);
        // In caso di errore (anche payload applicativo), resta in modifica.
        this.isEditing = true;
      },
    });
  }

  // Flatten di tutti i valori (inclusi children)
  private flattenSezioneValues(nodes: LabelNode[]): string[] {
    const out: string[] = [];
    const walk = (n: LabelNode) => {
      out.push(n.value);
      if (Array.isArray(n.children)) n.children.forEach(walk);
    };
    nodes.forEach(walk);
    // valori unici preservando ordine
    return Array.from(new Set(out));
  }

  private setEquals<T>(a: Set<T>, b: Set<T>): boolean {
    if (a.size !== b.size) return false;
    for (const v of a) if (!b.has(v)) return false;
    return true;
  }

  // Map StrutturaIndicePiaoDTO[] -> LabelNode[]
  private toLabelNodes(dtos: StrutturaIndicePiaoDTO[]): LabelNode[] {
    this.sezionePiaoMap.clear(); // pulisci la mappa precedente
    const toNode = (dto: StrutturaIndicePiaoDTO): LabelNode => {
      const numeroRaw = (dto as any).numeroSezione as string | undefined;
      const testo = (dto as any).testo as string | undefined;

      const numero = this.formatNumeroSezione(numeroRaw);
      const label = [numero, testo?.trim()].filter(Boolean).join(' ');

      // Popola la mappa: label -> DTO originale
      this.sezionePiaoMap.set(label, dto);

      const children = (dto as any).children as StrutturaIndicePiaoDTO[] | undefined;
      const node: LabelNode = { label, value: label };
      if (Array.isArray(children) && children.length) {
        node.children = children.map(toNode);
      }
      return node;
    };

    return Array.isArray(dtos) ? dtos.map(toNode) : [];
  }

  private formatNumeroSezione(s?: string): string {
    if (!s) return '';
    // if only digits, add dots between characters, else leave as-is
    return /^[0-9]+$/.test(s) ? s.split('').join('.') : s;
  }

  private unformatNumeroSezione(s?: string): string {
    if (!s) return '';
    // Remove dots from the string (inverse of formatNumeroSezione)
    return s.replace(/\./g, '');
  }
}
