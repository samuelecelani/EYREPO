import { Component, DestroyRef, inject, OnInit } from '@angular/core';
import { FormBuilder, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { OAuthService } from 'angular-oauth2-oidc';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { AuthService } from '../../shared/services/auth.service';
import { AppConfig } from '../../shared/models/interfaces/config.model';
import { getConfig, IPIAOConfig } from '../../shared/config/loader-config';
import { buildAuthConfig } from '../../shared/config/auth.config';

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrl: './login.component.scss',
  standalone: true,
  imports: [FormsModule, ReactiveFormsModule],
})
export class LoginComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly loginService = inject(AuthService);
  private readonly oAuthService = inject(OAuthService);
  private readonly route = inject(ActivatedRoute);
  private readonly destroyRef = inject(DestroyRef);

  protected config: IPIAOConfig = <IPIAOConfig>{};

  loginForm = this.fb.nonNullable.group({
    username: ['administrator', [Validators.required]],
    password: ['rootroot', [Validators.required]],
    rememberMe: [false],
    fakeUserCF: [undefined],
  });

  constructor() {
    this.config = getConfig();
  }

  ngOnInit(): void {
    this.route.queryParamMap
      .pipe(takeUntilDestroyed(this.destroyRef)) // auto-unsubscribe
      .subscribe((params) => {
        const redirect = params.get('redirect') || '';

        const config = buildAuthConfig(getConfig());
        this.oAuthService.configure(config);

        this.oAuthService.loadDiscoveryDocumentAndTryLogin().then(() => {
          if (this.oAuthService.hasValidAccessToken()) {
            // Prova a leggere ammId e route dal parametro state OIDC
            // (codificati da performRefresh in caso di token scaduto)
            let resolvedRedirect = redirect;
            let preferredAmmId: string | undefined;
            const rawState = this.oAuthService.state;
            if (rawState) {
              try {
                const parsed = JSON.parse(rawState);
                if (parsed.route) resolvedRedirect = parsed.route;
                if (parsed.ammId) preferredAmmId = String(parsed.ammId);
              } catch {
                // State non è il nostro JSON (es. route plain da preserveRequestedRoute)
                if (rawState.startsWith('/')) resolvedRedirect = rawState;
              }
            }
            this.loginService.afterLoginWithValidToken(resolvedRedirect, preferredAmmId);
          } else {
            this.loginService.loginIDP(redirect);
          }
        });
      });
  }

  get username() {
    return this.loginForm.get('username')!;
  }
  get getFakeUserCF() {
    return this.loginForm.get('fakeUserCF')!;
  }
  get password() {
    return this.loginForm.get('password')!;
  }
}
