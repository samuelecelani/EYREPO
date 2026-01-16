import { Component, inject, OnInit } from '@angular/core';
import { SharedModule } from '../../../module/shared/shared.module';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { UserProfile } from '../../../models/userProfile';
import { ButtonComponent } from '../../../components/button/button.component';
import { TextBoxComponent } from '../../../components/text-box/text-box.component';

@Component({
  selector: 'piao-profilo-utente',
  standalone: true,
  imports: [SharedModule, ButtonComponent, ReactiveFormsModule, TextBoxComponent],
  templateUrl: './profilo-utente.component.html',
  styleUrls: ['./profilo-utente.component.scss'],
})
export class ProfiloUtenteComponent implements OnInit {
  private fb = inject(FormBuilder);

  isEditing = false;

  data: UserProfile = {
    profiloUtente: 'Super User',
    sezionePiao: 'Tutte',
    amministrazione: 'Comune di Roma',
    nome: 'Mario',
    cognome: 'Rossi',
    codiceFiscale: 'RSSMRA80A01H501U',
    dataNascita: '01/01/1980',
    luogoNascita: 'Roma',
    emailIstituzionale: 'mario.rossi@example.com',
  };

  form = this.fb.group({
    profiloUtente: [''],
    sezionePiao: [''],
    amministrazione: [''],
    nome: [''],
    cognome: [''],
    codiceFiscale: [''],
    dataNascita: [''],
    luogoNascita: [''],
    emailIstituzionale: [''],
  });

  ngOnInit(): void {
    this.form.patchValue(this.data);
  }

  get v() {
    return this.form.getRawValue();
  }

  handleEditProfile() {
    this.isEditing = true;
  }

  handleSaveProfile() {
    this.isEditing = false;
    this.data = { ...this.form.getRawValue() } as UserProfile;
    console.log('Dati salvati:', this.data);
  }
}
