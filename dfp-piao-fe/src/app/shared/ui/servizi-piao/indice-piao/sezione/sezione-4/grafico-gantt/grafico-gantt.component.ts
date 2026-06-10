import {
  Component,
  ElementRef,
  inject,
  Input,
  NgZone,
  OnChanges,
  OnDestroy,
  SimpleChanges,
  ViewChild,
} from '@angular/core';
import { SharedModule } from '../../../../../../module/shared/shared.module';

import * as echarts from 'echarts/core';
import { CustomChart } from 'echarts/charts';
import {
  TooltipComponent,
  GridComponent,
  LegendComponent,
  MarkPointComponent,
} from 'echarts/components';
import { CanvasRenderer } from 'echarts/renderers';
import type { EChartsOption } from 'echarts';
import { GanttTask } from '../../../../../../models/interfaces/gant-task';

echarts.use([
  CustomChart,
  TooltipComponent,
  GridComponent,
  LegendComponent,
  MarkPointComponent,
  CanvasRenderer,
]);

@Component({
  selector: 'piao-grafico-gantt',
  imports: [SharedModule],
  templateUrl: './grafico-gantt.component.html',
  styleUrls: ['./grafico-gantt.component.scss'],
})
export class GraficoGanttComponent implements OnChanges, OnDestroy {
  private _chartEl?: ElementRef<HTMLDivElement>;
  private _pendingRaf?: number;

  @ViewChild('chartEl') set chartEl(el: ElementRef<HTMLDivElement> | undefined) {
    const changed = el?.nativeElement !== this._chartEl?.nativeElement;
    this._chartEl = el;
    if (el && changed) {
      // The DOM element just appeared (e.g. @if toggled).
      // Dispose any stale chart that pointed to the old DOM node.
      this.disposeChart();
      this.scheduleInit();
    }
  }

  private chart?: echarts.ECharts;
  private ngZone = inject(NgZone);

  @Input() tasks: GanttTask[] = [];

  @Input() maxDate!: string;

  @Input() minDate!: string;

  @Input() milestones: { date: string; name?: string }[] = [];

  private readonly COLORS = {
    fase: '#E6007E',
    sottofase: '#9B59F0',
    milestone: '#1A6CFF',
  };

  legendItems = [
    { label: 'Fase', color: '#E6007E', shape: 'circle' },
    { label: 'Sottofase', color: '#9B59F0', shape: 'circle' },
    { label: 'Milestone', color: '#1A6CFF', shape: 'diamond' },
  ];

  // ===== Lifecycle =====
  ngOnChanges(changes: SimpleChanges): void {
    const relevant =
      changes['tasks'] || changes['minDate'] || changes['maxDate'] || changes['milestones'];
    if (relevant) {
      if (this.chart) {
        this.applyOptionSafe();
      } else {
        this.scheduleInit();
      }
    }
  }

  /** Wait for the browser to finish layout, then init + render. */
  private scheduleInit(): void {
    if (this._pendingRaf) return;
    this._pendingRaf = requestAnimationFrame(() => {
      this._pendingRaf = undefined;
      this.initChart();
    });
  }

  private initChart(): void {
    if (!this._chartEl) return;
    this.ngZone.runOutsideAngular(() => {
      if (!this.chart) {
        this.chart = echarts.init(this._chartEl!.nativeElement);
        window.addEventListener('resize', this.handleResize);
      }
      // Force ECharts to pick up the element's actual size
      this.chart.resize();
      this.applyOption();
    });
  }

  private applyOptionSafe(): void {
    this.ngZone.runOutsideAngular(() => this.applyOption());
  }

  private disposeChart(): void {
    if (this.chart) {
      this.chart.dispose();
      this.chart = undefined;
    }
  }

  // ===== Helpers =====
  private handleResize = () => this.chart?.resize();

  private toTimestamp(dateStr: string): number {
    // Normalize 'YYYY-MM-DDTHH:mm:ss' → 'YYYY-MM-DD' to avoid timezone issues
    const normalized = dateStr.includes('T') ? dateStr.split('T')[0] : dateStr;
    return new Date(normalized + 'T00:00:00').getTime();
  }

  private computeAxisRange(): { min: number; max: number; interval: number; showDays: boolean } {
    const minTs = this.toTimestamp(this.minDate);
    const maxTs = this.toTimestamp(this.maxDate);
    const DAY = 24 * 3600 * 1000;
    const spanDays = (maxTs - minTs) / DAY;

    const minD = new Date(minTs);
    const maxD = new Date(maxTs);

    // Always start from 1st of minDate's month
    const axisMin = new Date(minD.getFullYear(), minD.getMonth(), 1).getTime();

    if (spanDays <= 90) {
      // Day-level ticks: interval scales with range
      const axisMax = new Date(maxD.getFullYear(), maxD.getMonth() + 1, 0).getTime();
      let interval: number;
      if (spanDays <= 31) {
        interval = 4 * DAY;
      } else if (spanDays <= 62) {
        interval = 7 * DAY;
      } else {
        interval = 10 * DAY;
      }
      return { min: axisMin, max: axisMax, interval, showDays: true };
    }

    // Long range (>90 days): monthly ticks
    const axisMax = new Date(maxD.getFullYear(), maxD.getMonth() + 1, 1).getTime();
    return { min: axisMin, max: axisMax, interval: 30 * DAY, showDays: false };
  }

  private applyOption(): void {
    if (!this.tasks.length || !this.minDate || !this.maxDate) return;

    const categories = this.tasks.map((t) => t.name);
    const barHeight = 14;

    // Combine bars + milestones into a single data array
    // Bars: [categoryIndex, startTs, endTs, 'bar']
    // Milestones: [categoryIndex, milestoneTs, 0, 'milestone']
    const allData: any[] = this.tasks.map((task, idx) => ({
      value: [idx, this.toTimestamp(task.start), this.toTimestamp(task.end), 'bar'],
      itemStyle: { color: this.COLORS[task.type] },
    }));

    for (const m of this.milestones) {
      allData.push({
        value: [0, this.toTimestamp(m.date), 0, 'milestone'],
        itemStyle: { color: this.COLORS.milestone },
      });
    }

    const axisRange = this.computeAxisRange();

    const option: EChartsOption = {
      tooltip: { show: false },
      grid: {
        left: 0,
        right: 40,
        top: 30,
        bottom: 50,
        containLabel: false,
      },
      xAxis: {
        type: 'value',
        position: 'bottom',
        axisLabel: {
          formatter: (value: number) => {
            const d = new Date(value);
            const months = [
              'Gen',
              'Feb',
              'Mar',
              'Apr',
              'Mag',
              'Giu',
              'Lug',
              'Ago',
              'Set',
              'Ott',
              'Nov',
              'Dic',
            ];
            if (axisRange.showDays) {
              return `${d.getDate()} ${months[d.getMonth()]}\n${d.getFullYear()}`;
            }
            return `${months[d.getMonth()]}\n${d.getFullYear()}`;
          },
          fontFamily: 'Titillium Web',
          fontSize: 12,
          color: '#1a1a1a',
        },
        axisLine: { show: false },
        axisTick: { show: false },
        splitLine: {
          show: true,
          lineStyle: { color: '#E8E8E8', type: 'solid' },
        },
        min: axisRange.min,
        max: axisRange.max,
        interval: axisRange.interval,
      },
      yAxis: {
        type: 'category',
        data: categories,
        inverse: true,
        axisLine: { show: false },
        axisTick: { show: false },
        axisLabel: {
          fontFamily: 'Titillium Web',
          fontSize: 13,
          formatter: (value: string) => {
            const task = this.tasks.find((t) => t.name === value);
            return task?.type === 'fase' ? `{fase|${value}}` : `{sottofase|${value}}`;
          },
          rich: {
            fase: {
              fontFamily: 'Titillium Web',
              fontSize: 16,
              fontWeight: 'bold',
              color: '#0066CC',
            },
            sottofase: {
              fontFamily: 'Titillium Web',
              fontSize: 15,
              fontWeight: 'normal',
              color: '#0066CC',
            },
          },
        },
        splitLine: {
          show: true,
          lineStyle: { color: '#E8E8E8', type: 'solid' },
        },
      },
      series: [
        {
          type: 'custom',
          renderItem: (_params: any, api: any) => {
            const categoryIndex = api.value(0);
            const type = api.value(3);

            if (type === 'milestone') {
              const coord = api.coord([api.value(1), categoryIndex]);
              const half = 14;
              return {
                type: 'polygon',
                shape: {
                  points: [
                    [coord[0], coord[1] - half],
                    [coord[0] + half, coord[1]],
                    [coord[0], coord[1] + half],
                    [coord[0] - half, coord[1]],
                  ],
                },
                style: { fill: api.style().fill || this.COLORS.milestone },
                z2: 10,
              };
            }

            // Bar
            const start = api.coord([api.value(1), categoryIndex]);
            const end = api.coord([api.value(2), categoryIndex]);
            const width = end[0] - start[0];
            return {
              type: 'rect',
              shape: {
                x: start[0],
                y: start[1] - barHeight / 2,
                width,
                height: barHeight,
                r: barHeight / 2,
              },
              style: api.style(),
            };
          },
          encode: {
            x: [1, 2],
            y: 0,
          },
          data: allData,
          silent: true,
          z: 2,
        },
      ],
    };

    this.chart!.setOption(option, { notMerge: true, lazyUpdate: true });

    // Tooltip via zrender mousemove (native tooltip crashes on custom series in ECharts v6)
    this.setupTooltip(categories);
  }

  private tooltipDiv?: HTMLDivElement;

  private setupTooltip(categories: string[]): void {
    if (!this.tooltipDiv) {
      this.tooltipDiv = document.createElement('div');
      Object.assign(this.tooltipDiv.style, {
        position: 'fixed',
        display: 'none',
        pointerEvents: 'none',
        background: '#fff',
        border: '1px solid #e0e0e0',
        borderRadius: '4px',
        padding: '8px 12px',
        fontFamily: "'Titillium Web', sans-serif",
        fontSize: '13px',
        color: '#333',
        boxShadow: '0 2px 8px rgba(0,0,0,0.15)',
        zIndex: '999',
        whiteSpace: 'nowrap',
        lineHeight: '1.6',
      });
      document.body.appendChild(this.tooltipDiv);
    }

    const barHeight = 14;
    const zr = this.chart!.getZr();
    zr.off('mousemove');
    zr.off('mouseout');
    zr.off('globalout');

    zr.on('mousemove', (e: any) => {
      const chartRect = this._chartEl!.nativeElement.getBoundingClientRect();
      const mx = e.offsetX;
      const my = e.offsetY;
      const lines: string[] = [];

      // Check each task bar using pixel coordinates
      for (let i = 0; i < this.tasks.length; i++) {
        const task = this.tasks[i];
        const sTs = this.toTimestamp(task.start);
        const eTs = this.toTimestamp(task.end);
        try {
          const startPx = this.chart!.convertToPixel({ xAxisIndex: 0, yAxisIndex: 0 }, [sTs, i]);
          const endPx = this.chart!.convertToPixel({ xAxisIndex: 0, yAxisIndex: 0 }, [eTs, i]);
          if (!startPx || !endPx) continue;
          const x1 = startPx[0];
          const x2 = endPx[0];
          const cy = startPx[1];
          if (mx >= x1 && mx <= x2 && my >= cy - barHeight && my <= cy + barHeight) {
            const color = this.COLORS[task.type];
            const startStr = new Date(sTs).toLocaleDateString('it-IT');
            const endStr = new Date(eTs).toLocaleDateString('it-IT');
            lines.push(
              `<span style="display:inline-block;width:8px;height:8px;border-radius:50%;background:${color};margin-right:6px"></span><b>${task.name}</b><br/>${startStr} \u2192 ${endStr}`
            );
          }
        } catch {
          /* skip */
        }
      }

      // Check milestones (on row 0)
      for (const m of this.milestones) {
        const mTs = this.toTimestamp(m.date);
        try {
          const mPx = this.chart!.convertToPixel({ xAxisIndex: 0, yAxisIndex: 0 }, [mTs, 0]);
          if (!mPx) continue;
          const half = 14;
          if (
            mx >= mPx[0] - half &&
            mx <= mPx[0] + half &&
            my >= mPx[1] - half &&
            my <= mPx[1] + half
          ) {
            const dateStr = new Date(mTs).toLocaleDateString('it-IT');
            lines.push(
              `<span style="display:inline-block;width:8px;height:8px;background:${this.COLORS.milestone};transform:rotate(45deg);margin-right:6px"></span><b>Milestone</b>: ${m.name ? m.name + ' — ' : ''}${dateStr}`
            );
          }
        } catch {
          /* skip */
        }
      }

      if (lines.length > 0) {
        this.tooltipDiv!.innerHTML = lines.join('<br/>');
        this.tooltipDiv!.style.display = 'block';
        // Measure tooltip width to flip side if near right edge
        const tipWidth = this.tooltipDiv!.offsetWidth || 150;
        const spaceRight = window.innerWidth - (chartRect.left + mx);
        if (spaceRight < tipWidth + 30) {
          this.tooltipDiv!.style.left = chartRect.left + mx - tipWidth - 15 + 'px';
        } else {
          this.tooltipDiv!.style.left = chartRect.left + mx + 15 + 'px';
        }
        this.tooltipDiv!.style.top = chartRect.top + my - 10 + 'px';
      } else {
        this.tooltipDiv!.style.display = 'none';
      }
    });

    const hide = () => {
      this.tooltipDiv!.style.display = 'none';
    };
    zr.on('mouseout', hide);
    zr.on('globalout', hide);
  }

  ngOnDestroy(): void {
    if (this._pendingRaf) cancelAnimationFrame(this._pendingRaf);
    window.removeEventListener('resize', this.handleResize);
    this.chart?.dispose();
    this.tooltipDiv?.remove();
  }
}
