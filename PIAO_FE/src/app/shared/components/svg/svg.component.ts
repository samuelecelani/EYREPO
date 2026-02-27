import { Component, Input, OnInit, OnChanges, SecurityContext, SimpleChanges } from '@angular/core';
import { CommonModule } from '@angular/common';
import { DomSanitizer, SafeHtml } from '@angular/platform-browser';
import { HttpClient } from '@angular/common/http';

@Component({
    selector: 'piao-svg',
    imports: [CommonModule],
    templateUrl: './svg.component.html',
    styleUrl: './svg.component.scss'
})
export class SvgComponent implements OnInit, OnChanges {
  @Input() icon!: string;
  @Input() svgContainer: string = 'svg-container';
  @Input() fillColor: string = '#000000'; // Colore di default
  iconPath: string = '/assets/icon/';
  fileExtension: string = '.svg';
  svgContent: SafeHtml = '';

  constructor(
    private http: HttpClient,
    private sanitizer: DomSanitizer
  ) { }

  ngOnInit(): void {
    if (this.icon) {
      this.iconPath = this.iconPath + this.icon + this.fileExtension;
      this.loadSvg();
    }
  }

  ngOnChanges(changes: SimpleChanges): void {
    // Ricarica l'SVG quando cambia il fillColor
    if (changes['fillColor'] && !changes['fillColor'].firstChange && this.icon) {
      this.loadSvg();
    }
  }

  private loadSvg(): void {
    this.http.get(this.iconPath, { responseType: 'text' }).subscribe({
      next: (svgText: string) => {
        // Sostituisce tutti i fill con il colore desiderato
        let modifiedSvg = svgText.replace(/fill="[^"]*"/g, `fill="${this.fillColor}"`);

        this.svgContent = this.sanitizer.bypassSecurityTrustHtml(modifiedSvg);
      },
      error: (error) => {
        console.error("Errore nel caricamento dell'icona:", error);
      },
    });
  }
}
