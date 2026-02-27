import { Component, inject, OnInit, OnDestroy, Input } from '@angular/core';
import { SharedModule } from '../../../module/shared/shared.module';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { UserProfile } from '../../../models/userProfile';
import { ButtonComponent } from '../../../components/button/button.component';
import { TextBoxComponent } from '../../../components/text-box/text-box.component';
import { DropdownComponent } from '../../../components/dropdown/dropdown.component';
import { LabelValue } from '../../../models/interfaces/label-value';
import { PIAOService } from '../../../services/piao.service';
import { GestionaleService } from '../../../services/gestionale.service';
import { AccountService } from '../../../services/account.service';
import { GestionaleStateService } from '../../../services/gestionale-state.service';
import { StrutturaIndicePiaoDTO } from '../../../models/classes/struttura-indice-piao-dto';
import { UtenteRuoloPaDTO } from '../../../models/classes/utente-ruolo-pa-dto';
import { PARiferimentoDTO } from '../../../models/classes/pa-riferimento-dto';
import { ActivatedRoute, Router } from '@angular/router';
import { map, of, catchError, takeUntil, Subject } from 'rxjs';
import { SelectedUserState } from '../../../models/interfaces/SelectedUserState';

type LabelNode = LabelValue & { children?: LabelNode[] };
type RuoloOption = LabelValue & { code: string };

@Component({
  selector: 'piao-profilo-utente',
  standalone: true,
  imports: [
    SharedModule,
    ButtonComponent,
    ReactiveFormsModule,
    TextBoxComponent,
    DropdownComponent,
  ],
  templateUrl: './profilo-utente.component.html',
  styleUrls: ['./profilo-utente.component.scss'],
})
export class ProfiloUtenteComponent implements OnInit, OnDestroy {
  @Input() userId: string | undefined;

  private fb = inject(FormBuilder);
  private piaoService: PIAOService = inject(PIAOService);
  private gestionaleService = inject(GestionaleService);
  private gestionaleStateService = inject(GestionaleStateService);
  private accountService = inject(AccountService);
  private activatedRoute = inject(ActivatedRoute);
  private router = inject(Router);
  private destroy$ = new Subject<void>();

  isEditing = false;
  isNewUser = false;
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

  profiloDropdownOptions: RuoloOption[] = [
    { label: 'Super User', value: 'Super User', code: '1001' },
    { label: 'Amministratore', value: 'Amministratore', code: '1002' },
    { label: 'Super Visore', value: 'Super Visore', code: '1003' },
    { label: 'Referente', value: 'Referente', code: '1234' },
    { label: 'Coordinatore Amministrativo', value: 'Coordinatore Amministrativo', code: '1004' },
    { label: 'Validatore', value: 'Validatore', code: '1005' },
    { label: 'Redattore', value: 'Redattore', code: '1006' },
  ];
  amministrazioneOptions: LabelValue[] = [
    { label: 'Comune di Roma', value: 'Comune di Roma' },
    { label: 'Comune di Milano', value: 'Comune di Milano' },
  ];
  form = this.fb.group({
    profiloUtente: [''],
    // usa array per multi-select
    sezionePiao: this.fb.control<string[]>([]),
    sezionePiaoDisplay: this.fb.control<string>(''), // display-only for read mode
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
    // Il path "/pages/gestionale/dettaglio-attivita/new" è statico, non un parametro
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

    // Carica paRiferimento da AccountService per ENTRAMBI i casi (nuovo e modifica)
    // Questo assicura che codicePA sia disponibile sia per nuovi che per utenti esistenti
    this.accountService
      .getAccount()
      .pipe(takeUntil(this.destroy$))
      .subscribe((user) => {
        if (user?.paRiferimento && user.paRiferimento.length > 0) {
          // Prendi la PA attiva (quella con attiva = true) o la prima
          const activePA = user.paRiferimento.find((pa) => pa.attiva) || user.paRiferimento[0];
          this.paRiferimento = activePA;
          console.log('paRiferimento loaded from AccountService:', this.paRiferimento);
        }
      });

    // Ascolta l'utente selezionato dallo stato condiviso (solo per modifica)
    this.gestionaleStateService.selectedUser$
      .pipe(takeUntil(this.destroy$))
      .subscribe((state: SelectedUserState | null) => {
        console.log('Selected user state from service:', state);
        if (state && !this.isNewUser) {
          this.selectedUser = state.user;
          this.paRiferimento = state.paRiferimento;
          // Usa setTimeout per assicurarsi che il form sia pronto
          setTimeout(() => {
            this.populateFormFromUser(state.user, state.paRiferimento);
          }, 0);
        }
      });
  }

  private loadSezionePiaoOptions(): void {
    this.piaoService
      .getStructureIndicePIAO()
      .pipe(
        map((dtos) => this.toLabelNodes(dtos)),
        catchError(() => of<LabelNode[]>([]))
      )
      .subscribe((options) => {
        this.sezionePiaoOptions = options;
        console.log('Sezioni PIAO options loaded:', options);
      });
  }

  //TODO: Appena avute le chiamate reali ai servizi di BIP, eliminare quei valori di default "di test"
  private populateFormFromUser(user: UtenteRuoloPaDTO, paRiferimento: PARiferimentoDTO): void {
    // Carica tutte le sezioni disponibili dal servizio
    this.piaoService
      .getStructureIndicePIAO()
      .pipe(
        map((dtos) => this.toLabelNodes(dtos)),
        catchError(() => of<LabelNode[]>([]))
      )
      .subscribe((allOptions) => {
        this.sezionePiaoOptions = allOptions;

        // Estrai le sezioni dell'utente dai sezioni array
        // Ogni elemento ha strutturaPiao con numeroSezione e testo
        const userSezioni: string[] = [];
        console.log('User sezioni raw:', user.sezioni);
        console.log('sezionePiaoMap size after toLabelNodes:', this.sezionePiaoMap.size);
        console.log('sezionePiaoMap keys:', Array.from(this.sezionePiaoMap.keys()));

        if (user.sezioni && Array.isArray(user.sezioni)) {
          user.sezioni.forEach((sezione) => {
            if (sezione.strutturaPiao) {
              const numero = this.formatNumeroSezione(sezione.strutturaPiao.numeroSezione);
              const label = [numero, sezione.strutturaPiao.testo?.trim()].filter(Boolean).join(' ');
              console.log('Processing sezione:', sezione.strutturaPiao, 'formatted label:', label);
              console.log('Label exists in map:', this.sezionePiaoMap.has(label));
              userSezioni.push(label);
            }
          });
        }

        const allSezioni = this.flattenSezioneValues(allOptions);
        console.log('All available sezioni:', allSezioni);
        console.log('User sezioni extracted:', userSezioni);

        // Confronta: se le sezioni dell'utente coprono tutte quelle disponibili
        const isAllSezioni = this.setEquals(new Set(userSezioni), new Set(allSezioni));
        console.log('Is all sezioni:', isAllSezioni);

        this.data = {
          profiloUtente: user.ruoli?.[0]?.nomeRuolo || 'Ruolo di test',
          sezionePiao: isAllSezioni ? 'Tutte' : userSezioni.join(', '),
          amministrazione: paRiferimento.denominazionePA || '',
          nome: user.nome || 'Nome di test',
          cognome: user.cognome || 'Cognome di test',
          codiceFiscale: user.codiceFiscale || '',
          dataNascita: '01/01/1970',
          luogoNascita: 'Comune di nascita ',
          emailIstituzionale: user.email || 'email@test.com',
        };

        this.form.patchValue({
          profiloUtente: this.data.profiloUtente,
          amministrazione: this.data.amministrazione,
          nome: this.data.nome,
          cognome: this.data.cognome,
          codiceFiscale: this.data.codiceFiscale,
          dataNascita: this.data.dataNascita,
          luogoNascita: this.data.luogoNascita,
          emailIstituzionale: this.data.emailIstituzionale,
          sezionePiao: isAllSezioni ? allSezioni : userSezioni,
          sezionePiaoDisplay: this.data.sezionePiao,
        });

        // Reset form state
        this.form.markAsPristine();
        this.form.markAsUntouched();

        console.log(
          'Form populated with user data:',
          this.data,
          'userSezioni:',
          userSezioni,
          'isAll:',
          isAllSezioni
        );
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

  handleEditProfile() {
    this.isEditing = true;
    this.piaoService
      .getStructureIndicePIAO() // idPiao optional
      .pipe(
        map((value: StrutturaIndicePiaoDTO[]) => this.toLabelNodes(value)),
        catchError(() => of<LabelNode[]>([]))
      )
      .subscribe((options) => {
        // Use only the options from BE; no "Tutte" item added here
        this.sezionePiaoOptions = options;
      });
    // Seed della multi-select da dati esistenti
    const current = this.data.sezionePiao;
    if (current === 'Tutte') {
      const all = this.flattenSezioneValues(this.sezionePiaoOptions);
      this.form.controls['sezionePiao'].setValue(all);
    } else if (typeof current === 'string') {
      const arr = current
        .split(',')
        .map((s) => s.trim())
        .filter(Boolean);
      this.form.controls['sezionePiao'].setValue(arr);
    }
  }

  handleChange(event?: any) {
    console.log('Valore cambiato:', event, this.form.getRawValue());
  }

  handleSaveProfile() {
    this.isEditing = false;
    const formValue = this.form.getRawValue();
    const selected = Array.isArray(formValue.sezionePiao) ? formValue.sezionePiao : [];
    const all = this.flattenSezioneValues(this.sezionePiaoOptions);
    const isAll = selected.includes('Tutte') || this.setEquals(new Set(selected), new Set(all));

    this.data = {
      ...formValue,
      sezionePiao: isAll ? 'Tutte' : selected.join(', '),
    } as UserProfile;

    // aggiorna il controllo di sola lettura
    this.form.controls['sezionePiaoDisplay'].setValue(this.data.sezionePiao as string);

    // Chiama l'API per salvare l'utente
    if (this.isNewUser || this.selectedUser?.id) {
      this.saveUserProfile(formValue);
    } else {
      console.error('No user selected');
    }
  }

  private saveUserProfile(formValue: any): void {
    // Estrai le sezioni PIAO selezionate
    const selected = Array.isArray(formValue.sezionePiao) ? formValue.sezionePiao : [];
    const all = this.flattenSezioneValues(this.sezionePiaoOptions);
    const isAll = selected.includes('Tutte') || this.setEquals(new Set(selected), new Set(all));

    // Se "Tutte", includi tutte le sezioni; altrimenti solo quelle selezionate
    const sezionePiaoToSave = isAll ? all : selected;

    // Converti le etichette in UtenteRuoliPaSezioneDTO[]
    // Usa la mappa per ottenere il DTO originale con numeroSezione corretto
    const sezioniToSave: any[] = [];
    sezionePiaoToSave.forEach((label: string) => {
      const originaleDto = this.sezionePiaoMap.get(label);
      if (originaleDto) {
        sezioniToSave.push({
          strutturaPiao: {
            id: originaleDto.id,
            numeroSezione: this.unformatNumeroSezione(originaleDto.numeroSezione), // numeroSezione senza punti per il payload
            testo: originaleDto.testo,
          },
        });
      }
    });

    // Usa il valore del ruolo dal form (non da this.data che potrebbe non essere valorizzato per nuovi utenti)
    const profiloUtente = formValue.profiloUtente || this.data.profiloUtente;

    // Cerca il codice del ruolo dalle opzioni
    const ruoloOption = this.profiloDropdownOptions.find(
      (opt) => opt.value === profiloUtente || opt.label === profiloUtente
    );
    const ruoloCode = ruoloOption?.code;

    // Mappa i dati del form a UtenteRuoloPaDTO
    // Per nuovo utente, non includiamo l'ID (il BE lo creerà)
    const utenteToSave: UtenteRuoloPaDTO = {
      ...(this.selectedUser?.id && { id: this.selectedUser.id }),
      nome: formValue.nome,
      cognome: formValue.cognome,
      codiceFiscale: formValue.codiceFiscale,
      email: formValue.emailIstituzionale,
      // Ruoli con codiceRuolo (non codice)
      ruoli: profiloUtente
        ? [{ nomeRuolo: profiloUtente, ...(ruoloCode && { codiceRuolo: ruoloCode }) }]
        : undefined,
      // Salva le sezioni
      sezioni: sezioniToSave.length > 0 ? sezioniToSave : undefined,
      // Aggiungi il codicePa dalla PA di riferimento con il nome dell'amministrazione
      ...(this.paRiferimento?.codePA && {
        codicePA: [
          {
            codicePa: this.paRiferimento.codePA,
            nome: this.paRiferimento.denominazionePA,
          },
        ],
      }),
    };

    this.gestionaleService
      .saveUtentePa(utenteToSave)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (savedUser) => {
          console.log('User profile saved successfully:', savedUser);

          // Per nuovi utenti, memorizza l'utente salvato e ripopola il form
          if (this.isNewUser && savedUser) {
            this.selectedUser = savedUser;
            this.isEditing = false; // passa a modalità read-only

            // Ripopola il form con i dati salvati
            setTimeout(() => {
              if (this.paRiferimento) {
                this.populateFormFromUser(savedUser, this.paRiferimento);
              }
            }, 0);
          } else {
            // Per utenti existenti, naviga a elenco attività
            this.router.navigate(['/pages/gestionale']);
          }
        },
        error: (err) => {
          console.error('Error saving user profile:', err);
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
