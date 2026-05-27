import {
  ChangeDetectorRef,
  Component,
  inject,
  Input,
  OnInit,
  OnChanges,
  SecurityContext,
  SimpleChanges,
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { DomSanitizer, SafeHtml } from '@angular/platform-browser';
import { HttpClient } from '@angular/common/http';
import { of, tap } from 'rxjs';
import { AssetService } from '../../services/asset.service';

@Component({
  selector: 'piao-svg',
  imports: [CommonModule],
  templateUrl: './svg.component.html',
  styleUrl: './svg.component.scss',
})
export class SvgComponent implements OnInit, OnChanges {
  /** Cache statica condivisa tra tutte le istanze: evita chiamate HTTP duplicate */
  private static svgCache = new Map<string, string>();

  @Input() icon!: string;
  @Input() svgContainer: string = 'svg-container';
  @Input() fillColor: string = '#000000'; // Colore di default
  iconPath: string = '';
  fileExtension: string = '.svg';
  svgContent: SafeHtml = '';

  private readonly asset = inject(AssetService);

  constructor(
    private http: HttpClient,
    private sanitizer: DomSanitizer,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    if (this.icon) {
      this.iconPath = this.asset.url(`icon/${this.icon}${this.fileExtension}`);
      this.loadSvg();
    }
  }

  ngOnChanges(changes: SimpleChanges): void {
    // Riapplica il colore quando cambia fillColor (senza nuova HTTP)
    if (changes['fillColor'] && !changes['fillColor'].firstChange && this.icon) {
      this.loadSvg();
    }
  }

  private loadSvg(): void {
    const cacheKey = `${this.iconPath}__${this.fillColor}`;
    const cached = SvgComponent.svgCache.get(cacheKey);

    if (cached) {
      this.svgContent = this.sanitizer.bypassSecurityTrustHtml(cached);
      return;
    }

    // Controlla se il testo SVG grezzo è già in cache (con un altro colore)
    const rawKey = this.iconPath;
    const rawCached = SvgComponent.svgCache.get(rawKey);

    const source$ = rawCached
      ? of(rawCached)
      : this.http
          .get(this.iconPath, { responseType: 'text', headers: { 'id-spinner': 'none' } })
          .pipe(tap((svgText) => SvgComponent.svgCache.set(rawKey, svgText)));

    source$.subscribe({
      next: (svgText: string) => {
        const modifiedSvg = svgText.replace(/fill="[^"]*"/g, `fill="${this.fillColor}"`);
        SvgComponent.svgCache.set(cacheKey, modifiedSvg);
        this.svgContent = this.sanitizer.bypassSecurityTrustHtml(modifiedSvg);
        this.cdr.markForCheck();
      },
      error: (error) => {
        console.error("Errore nel caricamento dell'icona:", error);
      },
    });
  }
}
