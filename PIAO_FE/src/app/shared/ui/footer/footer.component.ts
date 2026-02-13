import { Component } from '@angular/core';

@Component({
  selector: 'piao-footer',
  standalone: true,
  templateUrl: './footer.component.html',
  styleUrl: './footer.component.scss',
})
export class FooterComponent {
  // Angular template non permette 'new' direttamente: computiamo nel componente
  currentYear = new Date().getFullYear();
}


