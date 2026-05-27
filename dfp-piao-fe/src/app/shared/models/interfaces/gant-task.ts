export interface GanttTask {
  name: string;
  type: 'fase' | 'sottofase';
  start: string; // 'YYYY-MM-DD'
  end: string; // 'YYYY-MM-DD'
}
