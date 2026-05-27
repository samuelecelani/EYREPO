import {
  ChangeDetectorRef,
  Component,
  ElementRef,
  EventEmitter,
  inject,
  Input,
  OnInit,
  Output,
  ViewChild,
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
  @ViewChild('chartEl', { static: false }) chartEl!: ElementRef<HTMLDivElement>;
  private chart?: echarts.ECharts;

  @Input() graficoData!: GraficoSezione31DTO[];
  @Input() idPiao!: number;
  @Input() controls!: FormControl;
  @Input() isDettaglio: boolean = false;

  @Output() graficoLoaded = new EventEmitter<void>();

  private sezione31Service = inject(Sezione31Service);
  private cdr = inject(ChangeDetectorRef);

  labelsPie: string[] = [];
  colors: string[] = ['#FDB913', '#33C6C8', '#2F66B1', '#003B79', '#5F6E82', '#FFA219', '#E53C86'];
  centerTitle = 'Lorem Ipsum';
  centerIndex = 2; // indice del segmento “validate”
  labelsLegend: string[] = [];

  titleGrafico: string = 'SEZIONE_31.GRAFICO.GENERA';
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
        this.chart = echarts.init(this.chartEl.nativeElement);
        window.addEventListener('resize', this.handleResize);
      }
      this.recompute();
      this.applyOption();
    }
  }

  handleDwnGrafico(): void {
    this.sezione31Service.getGraficoSezione31(this.idPiao).subscribe((data) => {
      // Distruggi la vecchia istanza ECharts (il DOM verrà ricreato da @if)
      if (this.chart) {
        this.chart.dispose();
        this.chart = undefined;
      }

      if (data && data.length > 0) {
        this.graficoData = data;
        this.controls.setValue(true);
      } else {
        this.graficoData = [];
        this.controls.setValue(false);
      }

      this.graficoLoaded.emit();

      // Forza CD per risolvere @ViewChild sul nuovo DOM creato da @if
      this.cdr.detectChanges();
      setTimeout(() => this.initChart());
    });
  }

  // ===== Helpers =====
  private handleResize = () => this.chart?.resize();

  private recompute(): void {
    if (this.graficoData && this.graficoData.length > 0) {
      this.total = this.graficoData.reduce((a, b) => a + Number(b.value || 0), 0);
      this.validated = Number(this.graficoData[this.centerIndex]?.value || 0);
      this.labelsPie = this.graficoData.map((item) => item.key || '');
      this.labelsLegend = this.graficoData.map((item) => item.key || '');
    }
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
          padAngle: 4,
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
    this.chart?.dispose();
  }
}
