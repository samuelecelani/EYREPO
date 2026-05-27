import {
  Component,
  ElementRef,
  EventEmitter,
  HostListener,
  Input,
  OnChanges,
  OnDestroy,
  OnInit,
  Output,
  SimpleChanges,
} from '@angular/core';
import { FormControl, ReactiveFormsModule } from '@angular/forms';
import { Subject, Subscription, debounceTime, distinctUntilChanged } from 'rxjs';
import { SharedModule } from '../../module/shared/shared.module';

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
export class AutocompleteTextBoxComponent<T = unknown> implements OnInit, OnChanges, OnDestroy {
  @Input() label!: string;
  @Input() id: string = 'autocomplete';
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

  isOpen = false;
  hasFocus = false;

  private searchSubject = new Subject<string>();
  private searchSub?: Subscription;
  private valueSub?: Subscription;
  private suppressNextSearch = false;

  constructor(private elementRef: ElementRef) {}

  ngOnInit(): void {
    this.searchSub = this.searchSubject
      .pipe(debounceTime(this.debounceMs), distinctUntilChanged())
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
    if (changes['suggestions']) {
      const query = (this.control?.value ?? '').toString();
      if (this.hasFocus && query.length >= this.minChars) {
        this.isOpen = true;
      }
    }
  }

  ngOnDestroy(): void {
    this.searchSub?.unsubscribe();
    this.valueSub?.unsubscribe();
  }

  private subscribeToControl(): void {
    this.valueSub?.unsubscribe();
    if (!this.control) return;
    this.valueSub = this.control.valueChanges.subscribe((value: string | null) => {
      const v = (value ?? '').toString();
      this.searchSubject.next(v);
      if (v.length < this.minChars) {
        this.isOpen = false;
      }
    });
  }

  onFocus(): void {
    this.hasFocus = true;
    const query = (this.control?.value ?? '').toString();
    if (query.length >= this.minChars && this.suggestions.length > 0) {
      this.isOpen = true;
    }
  }

  onBlur(): void {
    this.hasFocus = false;
  }

  highlightMatch(text: string): string {
    const query = (this.control?.value ?? '').toString().trim();
    if (!query || query.length < this.minChars) return text;
    const escaped = query.replace(/[.*+?^${}()|[\]\\]/g, '\\$&');
    return text.replace(new RegExp(`(${escaped})`, 'gi'), '<strong>$1</strong>');
  }

  selectOption(opt: AutocompleteOption<T>): void {
    this.suppressNextSearch = true;
    this.control.setValue(opt.label);
    this.isOpen = false;
    this.selected.emit(opt);
  }

  @HostListener('document:click', ['$event'])
  onDocumentClick(event: Event): void {
    if (!this.elementRef.nativeElement.contains(event.target)) {
      this.isOpen = false;
    }
  }
}
