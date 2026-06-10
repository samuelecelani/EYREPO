import { Component, inject, OnInit } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'piao-dfp-placeholder',
  imports: [CommonModule, RouterLink],
  templateUrl: './dfp-placeholder.component.html',
  styleUrl: './dfp-placeholder.component.scss',
})
export class DfpPlaceholderComponent implements OnInit {
  private readonly route = inject(ActivatedRoute);

  title = 'Pagina DFP';
  description = 'Questa sezione e in preparazione per il profilo DFP.';

  ngOnInit(): void {
    const routeData = this.route.snapshot.data;
    this.title = routeData['title'] ?? this.title;
    this.description = routeData['description'] ?? this.description;
  }
}
