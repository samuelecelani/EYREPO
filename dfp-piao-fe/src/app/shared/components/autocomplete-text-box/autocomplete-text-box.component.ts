import {
  AfterViewChecked,
  Component,
  DestroyRef,
  ElementRef,
  EventEmitter,
  HostListener,
  Input,
  NgZone,
  OnChanges,
  OnDestroy,
  OnInit,
  Output,
  SimpleChanges,
  ViewChild,
  inject,
} from '@angular/core';
import { FormControl, ReactiveFormsModule } from '@angular/forms';
import { Subject, Subscription, debounceTime, distinctUntilChanged } from 'rxjs';
import { SharedModule } from '../../module/shared/shared.module';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';

export interface AutocompleteOption<T = unknown> {
  label: string;
  sublabel?: string;
  value: T;
}

@Component({
  selector: 'piao-autocomplete-text-box',
  imports: [SharedModule, ReactiveFormsModule],
  templateUrl: './autocomplete-text-box.component.html',
  styleUrl: './autocomplete-text-box.component.scss',
  host: {
    '[class]': 'containerClass',
  },
})
export class AutocompleteTextBoxComponent<T = unknown>
  implements OnInit, OnChanges, OnDestroy, AfterViewChecked
{
  private destroyRef = inject(DestroyRef);
  private ngZone = inject(NgZone);
  @Input() label!: string;
  // Id univoco di default per evitare collisioni quando più autocomplete
  // sono presenti nella stessa pagina senza un [id] esplicito.
  @Input() id: string = `piao-autocomplete-${++AutocompleteTextBoxComponent._instanceCounter}`;
  private static _instanceCounter = 0;
  @Input() control!: FormControl;
  @Input() inputClass: string = 'input';
  @Input() containerClass: string = '';
  @Input() placeholder: string = '';
  @Input() isLabelTranslate: boolean = true;
  @Input() required: boolean = false;
  @Input() isReadOnly: boolean = false;
  @Input() isDetails: boolean = false;
  @Input() minChars: number = 3;
  @Input() debounceMs: number = 300;
  @Input() suggestions: AutocompleteOption<T>[] = [];
  @Input() loading: boolean = false;
  @Input() noResultsLabel: string = 'Nessun risultato';
  @Input() iconPath?: string;

  @Output() searchChange = new EventEmitter<string>();
  @Output() cleared = new EventEmitter<void>();
  @Output() selected = new EventEmitter<AutocompleteOption<T>>();

  @ViewChild('inputRef') inputRef?: ElementRef<HTMLInputElement>;

  isOpen = false;
  hasFocus = false;

  // Quando l'utente interagisce con l'input (digita o ha il focus) impostiamo
  // questo flag a true. Finché è true, dopo ogni view check ci assicuriamo che
  // il focus rimanga sull'input, evitando che il re-render della popup (causato
  // dall'arrivo dei suggerimenti dall'API) faccia perdere il focus.
  private keepFocus = false;
  // Posizione del caret da ripristinare dopo l'eventuale refocus, per non
  // perdere il punto in cui l'utente sta digitando.
  private caretPosition: number | null = null;

  private searchSubject = new Subject<string>();
  private searchSub?: Subscription;
  private valueSub?: Subscription;
  private suppressNextSearch = false;

  constructor(private elementRef: ElementRef) {}

  ngOnInit(): void {
    this.searchSub = this.searchSubject
      .pipe(debounceTime(this.debounceMs), distinctUntilChanged())
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((query) => {
        if (this.suppressNextSearch) {
          this.suppressNextSearch = false;
          return;
        }
        if (query && query.length >= this.minChars) {
          this.searchChange.emit(query);
        } else {
          this.cleared.emit();
        }
      });

    this.subscribeToControl();
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['control'] && !changes['control'].firstChange) {
      this.subscribeToControl();
    }
    if (changes['suggestions'] || changes['loading']) {
      const query = (this.control?.value ?? '').toString();
      if (this.keepFocus && query.length >= this.minChars) {
        this.isOpen = true;
      }
    }
  }

  ngAfterViewChecked(): void {
    if (!this.keepFocus) return;
    const el = this.inputRef?.nativeElement;
    if (!el) return;
    if (document.activeElement === el) return;
    // Eseguiamo il refocus fuori dalla zone Angular per evitare di triggerare
    // un ulteriore ciclo di change detection.
    this.ngZone.runOutsideAngular(() => {
      el.focus({ preventScroll: true });
      if (this.caretPosition != null) {
        try {
          el.setSelectionRange(this.caretPosition, this.caretPosition);
        } catch {
          /* alcuni input type non supportano setSelectionRange */
        }
      }
    });
  }

  ngOnDestroy(): void {
    this.searchSub?.unsubscribe();
    this.valueSub?.unsubscribe();
  }

  private subscribeToControl(): void {
    this.valueSub?.unsubscribe();
    if (!this.control) return;
    this.valueSub = this.control.valueChanges
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((value: string | null) => {
        const v = (value ?? '').toString();
        // L'utente sta digitando: salviamo la posizione del caret e attiviamo
        // la retention del focus, così quando la risposta dell'API arriva e la
        // popup viene renderizzata l'input non perde il focus.
        const el = this.inputRef?.nativeElement;
        if (el && document.activeElement === el) {
          this.keepFocus = true;
          this.caretPosition = el.selectionStart;
        }
        this.searchSubject.next(v);
        if (v.length < this.minChars) {
          this.isOpen = false;
        }
      });
  }

  onFocus(): void {
    this.hasFocus = true;
    this.keepFocus = true;
    const query = (this.control?.value ?? '').toString();
    if (query.length >= this.minChars && this.suggestions.length > 0) {
      this.isOpen = true;
    }
  }

  onBlur(): void {
    this.hasFocus = false;
    // NON azzeriamo keepFocus qui: il blur può essere causato dal re-render
    // della popup durante una ricerca, e in quel caso ngAfterViewChecked
    // ripristinerà il focus. Il flag viene resettato solo quando l'utente
    // clicca esplicitamente fuori (vedi onDocumentClick) o seleziona un'opzione.
  }

  highlightMatch(text: string): string {
    const query = (this.control?.value ?? '').toString().trim();
    if (!query || query.length < this.minChars) return text;
    const escaped = query.replace(/[.*+?^${}()|[\]\\]/g, '\\$&');
    return text.replace(new RegExp(`(${escaped})`, 'gi'), '<strong>$1</strong>');
  }

  selectOption(opt: AutocompleteOption<T>): void {
    this.suppressNextSearch = true;
    this.keepFocus = false;
    this.caretPosition = null;
    this.control.setValue(opt.label);
    this.isOpen = false;
    this.selected.emit(opt);
  }

  @HostListener('document:click', ['$event'])
  onDocumentClick(event: Event): void {
    if (!this.elementRef.nativeElement.contains(event.target)) {
      this.isOpen = false;
      // L'utente ha cliccato fuori dal componente: ora possiamo davvero
      // rilasciare il focus.
      this.keepFocus = false;
      this.caretPosition = null;
    }
  }
}
