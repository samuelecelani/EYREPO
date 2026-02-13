import { SharedModule } from '../../../../module/shared/shared.module';
import { TooltipComponent as PiaoTooltipComponent } from '../../../../components/tooltip/tooltip.component';

import { Component, ElementRef, Input, ViewChild } from '@angular/core';

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
  selector: 'piao-graphic-indice-piao',
  imports: [SharedModule, PiaoTooltipComponent],
  templateUrl: './graphic-indice-piao.component.html',
  styleUrl: './graphic-indice-piao.component.scss',
})
export class GraphicIndicePiaoComponent {
  @ViewChild('chartEl', { static: true }) chartEl!: ElementRef<HTMLDivElement>;
  private chart?: echarts.ECharts;

  @Input() statusSezioniGrapich: number[] = [2, 3, 2, 1, 1];
  labelsPie: string[] = [
    '\nSezioni \ne sottosezioni compilate',
    '\nSezioni \ne sottosezioni in validazione',
    '\nSezioni \ne sottosezioni validate',
    '\nSezioni \ne sottosezioni da compilare',
    '\nSezioni \ne sottosezioni in compilazione',
  ];
  colors: string[] = ['#2F475E', '#8700D9', '#008055', '#AF7E00', '#0066CC'];
  centerTitle = 'Sezioni validate';
  centerIndex = 2; // indice del segmento “validate”
  chartTitle = 'SCRIVANIA_PA.GRAPHIC_INDICE_PIAO.TITLE';
  legendTitle = 'SCRIVANIA_PA.GRAPHIC_INDICE_PIAO.LEGEND';
  labelsLegend: string[] = [
    'SCRIVANIA_PA.GRAPHIC_INDICE_PIAO.LABELS.COMPILATION',
    'SCRIVANIA_PA.GRAPHIC_INDICE_PIAO.LABELS.IN_VALIDATION',
    'SCRIVANIA_PA.GRAPHIC_INDICE_PIAO.LABELS.VALIDATE',
    'SCRIVANIA_PA.GRAPHIC_INDICE_PIAO.LABELS.TO_COMPILATION',
    'SCRIVANIA_PA.GRAPHIC_INDICE_PIAO.LABELS.IN_COMPILATION',
  ];

  // totale sezione
  total = 9;
  validated = 3;

  // ===== Lifecycle =====
  ngAfterViewInit(): void {
    this.chart = echarts.init(this.chartEl.nativeElement);
    this.recompute();
    this.applyOption();
    window.addEventListener('resize', this.handleResize);
  }

  ngOnChanges(): void {
    this.recompute();
    if (this.chart) this.applyOption();
  }

  // ===== Helpers =====
  private handleResize = () => this.chart?.resize();

  private recompute(): void {
    this.total = this.statusSezioniGrapich.reduce((a, b) => a + b, 0);
    this.validated = this.statusSezioniGrapich[this.centerIndex] ?? 0;
  }

  private applyOption(): void {
    const data = this.statusSezioniGrapich.map((v, i) =>
      v !== 0
        ? {
            value: v,
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
        subtext: `${this.validated}/${this.total}`,
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
          radius: ['45%', '60%'], // Inner e outer radius per il donut
          center: ['50%', '50%'],
          startAngle: -190,
          padAngle: 10,
          avoidLabelOverlap: true,
          itemStyle: {
            borderRadius: 20,
            borderColor: '#fff',
            borderWidth: 1,
          },
          label: {
            show: true,
            position: 'outer',
            formatter: (params: any) => {
              const wrap = this.wrapByChars(params.name);
              return params.value != 0 ? `{name|${wrap}}\n{value|${params.value}\n}` : '';
            },
            rich: {
              name: {
                fontSize: 16,
                color: '#000000',
                fontFamily: 'Titillium Web',
              },
              value: {
                fontSize: 18,
                fontWeight: 'bold',
                color: '#000000',
                fontFamily: 'Titillium Web',
              },
            },
          },
          labelLine: {
            show: true,
            length: 35,
            length2: 0,
            lineStyle: {
              width: 0,
            },
          },
          labelLayout: {
            dx: 5,
            dy: 5,
          },
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
