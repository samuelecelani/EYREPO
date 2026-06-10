import {
  ChangeDetectorRef,
  Component,
  DestroyRef,
  ElementRef,
  EventEmitter,
  Input,
  NgZone,
  OnInit,
  Output,
  ViewChild,
  inject,
} from '@angular/core';
import * as echarts from 'echarts/core';
import { PieChart } from 'echarts/charts';
import {
  TitleComponent,
  TooltipComponent,
  LegendComponent,
  GraphicComponent,
} from 'echarts/components';
import { CanvasRenderer } from 'echarts/renderers';
import type { EChartsOption } from 'echarts';
import { LabelLayout } from 'echarts/features';
import { SharedModule } from '../../../../../../module/shared/shared.module';
import { GraficoSezione31DTO } from '../../../../../../models/classes/grafico-sezione-31-dto';
import { Sezione31Service } from '../../../../../../services/sezione31.service';
import { FormControl } from '@angular/forms';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { isStatoMinervaAttivo } from '../../../../../../utils/utils';

echarts.use([
  PieChart,
  TitleComponent,
  TooltipComponent,
  LegendComponent,
  GraphicComponent,
  CanvasRenderer,
  LabelLayout,
]);

@Component({
  selector: 'piao-grafico-organico-amministrazione',
  imports: [SharedModule],
  templateUrl: './grafico-organico-amministrazione.component.html',
  styleUrl: './grafico-organico-amministrazione.component.scss',
})
export class GraficoOrganicoAmministrazioneComponent {
  private destroyRef = inject(DestroyRef);
  private ngZone = inject(NgZone);
  @ViewChild('chartEl', { static: false }) chartEl!: ElementRef<HTMLDivElement>;
  private chart?: echarts.ECharts;
  private resizeObserver?: ResizeObserver;
  private resizeRafId: number | null = null;

  @Input() graficoData!: GraficoSezione31DTO[];
  @Input() idEntitaFK!: number;
  @Input() codiceIpa!: string;
  @Input() statoSezione!: string;
  @Input() controls!: FormControl;
  @Input() isDettaglio: boolean = false;

  @Output() graficoLoaded = new EventEmitter<void>();

  private sezione31Service = inject(Sezione31Service);
  private cdr = inject(ChangeDetectorRef);

  labelsPie: string[] = [];
  /**
   * Palette base (7 colori EY/DFP). Quando i profili sono più dei colori base
   * usiamo `getColors()` per generare una palette estesa dinamicamente:
   * - prima cicla la base,
   * - poi genera nuove tinte distribuite via HSL (golden-angle) per evitare
   *   collisioni visive ed avere sempre colori distinti anche con N=52, 100…
   */
  basePalette: string[] = [
    '#FDB913',
    '#33C6C8',
    '#2F66B1',
    '#003B79',
    '#5F6E82',
    '#FFA219',
    '#E53C86',
  ];
  colors: string[] = [...this.basePalette];
  centerTitle = 'Profili di ruolo';
  centerIndex = 2; // indice del segmento “validate”
  labelsLegend: string[] = [];

  titleGrafico: string = 'SEZIONE_31.GRAFICO.GENERA';
  labelGraficoNotFound: string = 'SEZIONE_31.GRAFICO.NOT_FOUND';
  graficoNotFound: boolean = false;
  labelDettaglio: string = 'SEZIONE_31.ACCORDION_1.RAPP_GRAFICA.LABEL_DETTAGLIO';

  // totale sezione
  total = 9;
  validated = 3;

  // ===== Lifecycle =====
  ngAfterViewInit(): void {
    this.initChart();
  }

  ngOnChanges(): void {
    if (this.graficoData && this.graficoData.length > 0) {
      this.recompute();
      if (this.chart) {
        this.applyOption();
      } else {
        // DOM non ancora pronto, aspetta il prossimo ciclo di change detection
        setTimeout(() => this.initChart());
      }
    }
  }

  private initChart(): void {
    if (this.chartEl && this.graficoData && this.graficoData.length > 0) {
      if (!this.chart) {
        // Init / event listeners / resize observer FUORI dalla Angular Zone:
        // ECharts è self-contained (canvas) e i resize non devono triggerare CD
        // sull'app. Così evitiamo tick di change-detection inutili durante
        // l'apertura dell'accordion o il caricamento dei web font.
        this.ngZone.runOutsideAngular(() => {
          this.chart = echarts.init(this.chartEl.nativeElement);
          window.addEventListener('resize', this.handleResize);
          if (typeof ResizeObserver !== 'undefined') {
            this.resizeObserver = new ResizeObserver(() => this.scheduleResize());
            this.resizeObserver.observe(this.chartEl.nativeElement);
          }
        });
      }
      this.recompute();
      this.applyOption();
    }
  }

  /**
   * Debounce dei resize via requestAnimationFrame: ECharts può ricevere più
   * notifiche consecutive (font load, layout reflow, transizioni accordion).
   * Coalescing su rAF garantisce 1 sola chiamata reale a `chart.resize()` per
   * frame, eseguita fuori dalla Angular Zone.
   */
  private scheduleResize(): void {
    if (this.resizeRafId !== null) return;
    this.ngZone.runOutsideAngular(() => {
      this.resizeRafId = requestAnimationFrame(() => {
        this.resizeRafId = null;
        this.chart?.resize();
      });
    });
  }

  handleDwnGrafico(): void {
    this.sezione31Service
      .getGraficoSezione31(this.idEntitaFK, this.codiceIpa, isStatoMinervaAttivo(this.statoSezione))
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((data) => {
        // Distruggi la vecchia istanza ECharts (il DOM verrà ricreato da @if)
        if (this.chart) {
          this.chart.dispose();
          this.chart = undefined;
        }

        if (data && data.length > 0) {
          this.graficoData = data;
          this.controls.setValue(true);
          this.graficoNotFound = false;
        } else {
          this.graficoData = [];
          this.controls.setValue(false);
          this.graficoNotFound = true;
        }

        this.graficoLoaded.emit();

        // Forza CD per risolvere @ViewChild sul nuovo DOM creato da @if
        this.cdr.detectChanges();
        setTimeout(() => this.initChart());
      });
  }

  // ===== Helpers =====
  private handleResize = () => this.scheduleResize();

  private recompute(): void {
    if (this.graficoData && this.graficoData.length > 0) {
      this.total = this.graficoData.reduce((a, b) => a + Number(b.value || 0), 0);
      this.validated = Number(this.graficoData[this.centerIndex]?.value || 0);
      this.labelsPie = this.graficoData.map((item) => item.key || '');
      this.labelsLegend = this.graficoData.map((item) => item.key || '');
      // Adatta la palette al numero effettivo di profili (può essere 2, 52, …)
      this.colors = this.getColors(this.graficoData.length);
    }
  }

  /**
   * Restituisce un array di `count` colori distinti.
   * - Usa la palette base finché possibile.
   * - Oltre la palette base genera nuove tinte via HSL con il "golden angle"
   *   (~137.508°) per massimizzare la separazione visiva ed evitare colori simili.
   */
  private getColors(count: number): string[] {
    const out: string[] = [];
    const baseLen = this.basePalette.length;
    for (let i = 0; i < count; i++) {
      if (i < baseLen) {
        out.push(this.basePalette[i]);
      } else {
        const k = i - baseLen;
        const hue = (k * 137.508) % 360; // golden angle: distribuzione massimamente sparsa
        // alterna saturazione/luminosità per non avere blocchi monocromatici
        const sat = 55 + (k % 3) * 10; // 55, 65, 75
        const light = 50 + (k % 2) * 8; // 50, 58
        out.push(this.hslToHex(hue, sat, light));
      }
    }
    return out;
  }

  private hslToHex(h: number, s: number, l: number): string {
    s /= 100;
    l /= 100;
    const k = (n: number) => (n + h / 30) % 12;
    const a = s * Math.min(l, 1 - l);
    const f = (n: number) =>
      Math.round(255 * (l - a * Math.max(-1, Math.min(k(n) - 3, Math.min(9 - k(n), 1)))));
    const toHex = (v: number) => v.toString(16).padStart(2, '0');
    return `#${toHex(f(0))}${toHex(f(8))}${toHex(f(4))}`;
  }

  private applyOption(): void {
    const data = this.graficoData.map((v, i) =>
      Number(v.value) !== 0
        ? {
            value: Number(v.value),
            name: this.labelsPie[i],
            itemStyle: {
              color: this.colors[i],
              borderColor: '#ffffff',
              borderWidth: 1,
            },
          }
        : {}
    );

    const option: EChartsOption = {
      tooltip: {
        trigger: 'item',
        formatter: (params: any) => {
          const label = params.name;
          const value = params.value;
          const perc = this.total ? (value / this.total) * 100 : 0;
          return `${label}: ${value} (${perc.toFixed(0)}%)`;
        },
      },
      legend: { show: false },

      // Testo centrale (due righe)
      title: {
        text: this.centerTitle,
        subtext: `${this.total}`,
        left: 'center',
        top: '43%',
        textStyle: {
          fontSize: 20,
          fontWeight: 'normal',
          color: '#4A4A4A',
          fontFamily: 'Titillium Web',
        },
        subtextStyle: {
          fontSize: 28,
          fontWeight: 'bold',
          color: '#111',
          fontFamily: 'Titillium Web',
          width: 25,
        },
      },
      series: [
        {
          type: 'pie',
          radius: ['55%', '75%'],
          center: ['50%', '50%'],
          startAngle: 80,
          // padAngle dinamico: con tante fette (es. 52 profili) un padAngle fisso
          // di 4° consumerebbe 4° × N = 208° su 360° totali, schiacciando
          // le fette piccole a zero pixel. Riduciamo lo spacing in proporzione.
          padAngle: data.length > 20 ? 1 : data.length > 10 ? 2 : 4,
          // minAngle garantisce che ogni profilo presente nei dati abbia sempre
          // una fetta visibile, anche con valore 0 o 1%.
          minAngle: data.length > 30 ? 2 : 3,
          avoidLabelOverlap: true,
          itemStyle: {
            borderRadius: 6,
            borderColor: '#fff',
            borderWidth: 2,
          },
          label: { show: false },
          labelLine: { show: false },
          emphasis: {
            itemStyle: {
              shadowBlur: 10,
              shadowOffsetX: 0,
              shadowColor: 'rgba(0, 0, 0, 0.5)',
            },
          },
          data,
        },
      ],
    };

    this.chart!.setOption(option, { notMerge: true, lazyUpdate: true });
  }

  wrapByChars(str: string, maxChars = 24): string {
    const words = str.split(' ');
    const lines: string[] = [];
    let line = '';

    for (const w of words) {
      const candidate = line ? `${line} ${w}` : w;
      if (candidate.length > maxChars) {
        if (line) lines.push(line);
        line = w;
      } else {
        line = candidate;
      }
    }
    if (line) lines.push(line);
    return lines.join('\n'); // <-- ECharts interpreta \n come nuova riga
  }

  ngOnDestroy(): void {
    window.removeEventListener('resize', this.handleResize);
    this.resizeObserver?.disconnect();
    this.resizeObserver = undefined;
    if (this.resizeRafId !== null) {
      cancelAnimationFrame(this.resizeRafId);
      this.resizeRafId = null;
    }
    this.chart?.dispose();
  }
}
